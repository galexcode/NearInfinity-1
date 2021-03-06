// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.datatype;

import infinity.util.DynamicArray;

import java.io.IOException;
import java.io.OutputStream;

public class DecNumber extends Datatype implements InlineEditable
{
  private int number;

  public DecNumber(byte buffer[], int offset, int length, String name)
  {
    super(offset, length, name);
    number = 0;
    if (length == 4)
      number = DynamicArray.getInt(buffer, offset);
    else if (length == 2)
      number = (int)DynamicArray.getShort(buffer, offset);
    else if (length == 1)
      number = (int)DynamicArray.getByte(buffer, offset);
    else
      throw new IllegalArgumentException();
  }

// --------------------- Begin Interface InlineEditable ---------------------

  @Override
  public boolean update(Object value)
  {
    try {
      int newnumber = Integer.parseInt(value.toString());
      if (newnumber > Math.pow((double)2, (double)(8 * getSize() - 1)))
        return false;
      number = newnumber;
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return false;
  }

// --------------------- End Interface InlineEditable ---------------------


// --------------------- Begin Interface Writeable ---------------------

  @Override
  public void write(OutputStream os) throws IOException
  {
    super.writeInt(os, number);
  }

// --------------------- End Interface Writeable ---------------------

  @Override
  public String toString()
  {
    return Integer.toString(number);
  }

  public int getValue()
  {
    return number;
  }

  public void incValue(int value)
  {
    number += value;
  }

  public void setValue(int value)
  {
    number = value;
  }
}

