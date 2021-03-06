package com.revolsys.gis.postgresql.type;

import java.io.IOException;
import java.io.InputStream;

import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.util.ExceptionUtil;

public abstract class ValueGetter {
  protected static class BigEndian extends ValueGetter {

    public BigEndian(final InputStream data) {
      super(data, true);
    }

    @Override
    public int getInt() {
      try {
        return this.data.readInt();
      } catch (final IOException e) {
        return ExceptionUtil.throwUncheckedException(e);
      }
    }

    @Override
    public long getLong() {
      try {
        return this.data.readLELong();
      } catch (final IOException e) {
        return ExceptionUtil.throwUncheckedException(e);
      }
    }
  }

  protected static class LittleEndian extends ValueGetter {
    public LittleEndian(final InputStream data) {
      super(data, false);
    }

    @Override
    public int getInt() {
      try {
        return this.data.readLEInt();
      } catch (final IOException e) {
        return ExceptionUtil.throwUncheckedException(e);
      }
    }

    @Override
    public long getLong() {
      try {
        return this.data.readLELong();
      } catch (final IOException e) {
        return ExceptionUtil.throwUncheckedException(e);
      }
    }
  }

  public static ValueGetter create(final InputStream in) {
    try {
      final int endianType = in.read();
      if (endianType == 0) { // BigEndian
        return new BigEndian(in);
      } else if (endianType == 1) {
        return new LittleEndian(in);
      } else {
        throw new IllegalArgumentException("Unknown Endian type:" + endianType);
      }
    } catch (final IOException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  protected EndianInputStream data;

  private final boolean bigEndian;

  public ValueGetter(final InputStream data, final boolean bigEndian) {
    this.data = new EndianInputStream(data);
    this.bigEndian = bigEndian;
  }

  /**
   * Get a byte, should be equal for all endians
   */
  public byte getByte() {
    try {
      return this.data.readByte();
    } catch (final IOException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  public double getDouble() {
    final long bitrep = getLong();
    return Double.longBitsToDouble(bitrep);
  }

  public abstract int getInt();

  public abstract long getLong();

  public boolean isBigEndian() {
    return this.bigEndian;
  }
}
