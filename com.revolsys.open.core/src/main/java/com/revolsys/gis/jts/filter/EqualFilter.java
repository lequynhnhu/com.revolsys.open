/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

public class EqualFilter<T extends Geometry> implements Filter<T> {
  private final T geometry;

  public EqualFilter(final T geometry) {
    this.geometry = geometry;
  }

  @Override
  public boolean accept(final T geometry) {
    if (this.geometry.equals(geometry)) {
      return true;
    } else {
      return false;
    }
  }

  public T getGeometry() {
    return this.geometry;
  }
}
