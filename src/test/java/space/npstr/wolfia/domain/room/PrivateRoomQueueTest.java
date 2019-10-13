/*
 * Copyright (C) 2016-2019 Dennis Neufeld
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package space.npstr.wolfia.domain.room;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import space.npstr.wolfia.ApplicationTest;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrivateRoomQueueTest extends ApplicationTest {

    private static final Logger log = LoggerFactory.getLogger(PrivateRoomQueueTest.class);

    @Autowired
    private PrivateRoomQueue privateRoomQueue;


    @Test
    void queueShouldInitializeFromService() {
        PrivateRoomService service = mock(PrivateRoomService.class);
        PrivateRoom privateRoom1 = new PrivateRoom(uniqueLong(), 1);
        PrivateRoom privateRoom2 = new PrivateRoom(uniqueLong(), 1);
        when(service.getAll()).thenReturn(List.of(privateRoom1, privateRoom2));

        var queue = new PrivateRoomQueue(service);

        verify(service).getAll();
        List<ManagedPrivateRoom> rooms = queue.getAllManagedRooms();
        assertThat(rooms.size()).isEqualTo(2);
        assertThat(rooms).filteredOnAssertions(isRoom(privateRoom1)).size().isEqualTo(1);
        assertThat(rooms).filteredOnAssertions(isRoom(privateRoom2)).size().isEqualTo(1);
    }

    @Test
    void givenNoPrivateRoomsExist_pollReturnsEmpty() {
        Optional<ManagedPrivateRoom> poll = this.privateRoomQueue.poll();

        assertThat(poll).isEmpty();
    }

    @Test
    void givenPrivateRoomExists_pollReturnsRoom() {
        PrivateRoom privateRoom = new PrivateRoom(uniqueLong(), 1);
        this.privateRoomQueue.add(privateRoom);

        Optional<ManagedPrivateRoom> poll = this.privateRoomQueue.poll();

        assertThat(poll).hasValueSatisfying(isRoom(privateRoom));
    }

    @Test
    void givenNoPrivateRoomsExist_takeBlocks() {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicReference<ManagedPrivateRoom> taken = new AtomicReference<>();
        AtomicBoolean done = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                started.set(true);
                taken.set(this.privateRoomQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.set(true);
            }
        });

        thread.start();
        sleep(TWO_HUNDRED_MILLISECONDS);

        assertThat(started).isTrue();
        assertThat(thread.isAlive()).isTrue();
        assertThat(taken.get()).isNull();
        assertThat(done.get()).isFalse();

        thread.interrupt();
        sleep(ONE_HUNDRED_MILLISECONDS);

        assertThat(thread.isAlive()).isFalse();
        assertThat(taken.get()).isNull();
        assertThat(done.get()).isTrue();
    }


    @Test
    //avoid tests getting stuck in the blocking call
    @Timeout(5)
    void givenPrivateRoomExists_takeReturnsRoom() throws InterruptedException {
        PrivateRoom privateRoom = new PrivateRoom(uniqueLong(), 1);
        this.privateRoomQueue.add(privateRoom);

        ManagedPrivateRoom take = this.privateRoomQueue.take();

        assertThat(take).isNotNull();
        assertThat(take).satisfies(isRoom(privateRoom));
    }

    @Test
    void givenBlockingOnTake_whenRoomIsAdded_takeReturnsRoom() {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicReference<ManagedPrivateRoom> taken = new AtomicReference<>();
        AtomicBoolean done = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                started.set(true);
                taken.set(this.privateRoomQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.set(true);
            }
        });

        thread.start();
        sleep(TWO_HUNDRED_MILLISECONDS);

        assertThat(started).isTrue();
        assertThat(thread.isAlive()).isTrue();
        assertThat(taken.get()).isNull();
        assertThat(done.get()).isFalse();

        PrivateRoom privateRoom = new PrivateRoom(uniqueLong(), 1);
        this.privateRoomQueue.add(privateRoom);
        sleep(ONE_HUNDRED_MILLISECONDS);

        assertThat(thread.isAlive()).isFalse();
        assertThat(taken.get()).satisfies(isRoom(privateRoom));
        assertThat(done.get()).isTrue();
    }


    @Test
    void givenBlockingOnTake_whenRoomIsPutBack_takeReturnsRoom() {
        PrivateRoom privateRoom = new PrivateRoom(uniqueLong(), 1);
        this.privateRoomQueue.add(privateRoom);
        ManagedPrivateRoom busy = this.privateRoomQueue.poll().orElseThrow();

        AtomicBoolean started = new AtomicBoolean(false);
        AtomicReference<ManagedPrivateRoom> taken = new AtomicReference<>();
        AtomicBoolean done = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                started.set(true);
                taken.set(this.privateRoomQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.set(true);
            }
        });

        thread.start();
        sleep(TWO_HUNDRED_MILLISECONDS);

        assertThat(started).isTrue();
        assertThat(thread.isAlive()).isTrue();
        assertThat(taken.get()).isNull();
        assertThat(done.get()).isFalse();

        this.privateRoomQueue.putBack(busy);
        sleep(ONE_HUNDRED_MILLISECONDS);

        assertThat(thread.isAlive()).isFalse();
        assertThat(taken.get()).satisfies(isRoom(privateRoom));
        assertThat(done.get()).isTrue();
    }

    private void sleep(Duration duration) {
        long started = System.currentTimeMillis();
        await().until(() -> System.currentTimeMillis() - started > duration.toMillis());

        log.info("Slept for {}ms", System.currentTimeMillis() - started);
    }

    private Consumer<ManagedPrivateRoom> isRoom(PrivateRoom pr) {
        return actual -> {
            assertThat(actual.getGuildId()).isEqualTo(pr.getGuildId());
            assertThat(actual.getNumber()).isEqualTo(pr.getNumber());
        };
    }

}