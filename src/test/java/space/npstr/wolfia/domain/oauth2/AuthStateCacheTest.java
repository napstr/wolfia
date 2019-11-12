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

package space.npstr.wolfia.domain.oauth2;

import io.prometheus.client.cache.caffeine.CacheMetricsCollector;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static space.npstr.wolfia.TestUtil.uniqueLong;

class AuthStateCacheTest {

    private final AuthStateCache cache = new AuthStateCache(mock(CacheMetricsCollector.class));

    @Test
    void whenGetNullAuthState_returnEmpty() {
        Optional<AuthState> opt = cache.getAuthState(null);

        assertThat(opt).isEmpty();
    }

    @Test
    void whenGetUncachedAuthState_returnEmpty() {
        Optional<AuthState> opt = cache.getAuthState("bla");

        assertThat(opt).isEmpty();
    }

    @Test
    void whenGetExistingAuthState_returnAuthState() {
        long userId = uniqueLong();
        String redirectUrl = "https://example.org";
        AuthState authState = ImmutableAuthState.builder()
                .userId(userId)
                .redirectUrl(redirectUrl)
                .build();

        String state = cache.generateStateParam(authState);

        Optional<AuthState> opt = cache.getAuthState(state);

        assertThat(opt).hasValueSatisfying(actual -> {
            assertThat(actual.userId()).isEqualTo(actual.userId());
            assertThat(actual.redirectUrl()).isEqualTo(actual.redirectUrl());
        });
    }

}
