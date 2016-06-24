/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.util;

/**
 * A function with five arguments.
 *
 * @param <S> the first argument type
 * @param <T> the second argument type
 * @param <U> the third argument type
 * @param <V> the forth argument type
 * @param <W> the fifth argument type
 * @param <R> the result type
 */
@FunctionalInterface
public interface PentaFunction<S, T, U, V, W, R> {
	/**
	 * Applies this function to the given arguments.
	 *
	 * @param s the first function argument
	 * @param t the second function argument
	 * @param u the third function argument
	 * @param v the forth function argument
	 * @param w the fifth function argument
	 * @return the function result
	 */
	R apply(final S s, final T t, final U u, final V v, final W w);
}
