// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.resource.are;

import infinity.datatype.Bitmap;
import infinity.datatype.DecNumber;
import infinity.datatype.TextString;
import infinity.datatype.Unknown;
import infinity.resource.AbstractStruct;
import infinity.resource.AddRemovable;

public final class AutomapNotePST extends AbstractStruct implements AddRemovable
{
  private static final String[] s_yesno = new String[] { "No", "Yes" };

  AutomapNotePST() throws Exception
  {
    super(null, "Automap note", new byte[532], 0);
  }

  AutomapNotePST(AbstractStruct superStruct, byte buffer[], int offset, int number) throws Exception
  {
    super(superStruct, "Automap note " + number, buffer, offset);
  }

//--------------------- Begin Interface AddRemovable ---------------------

  @Override
  public boolean canRemove()
  {
    return true;
  }

//--------------------- End Interface AddRemovable ---------------------

  @Override
  protected int read(byte buffer[], int offset) throws Exception
  {
    list.add(new DecNumber(buffer, offset, 4, "Coordinate: X"));
    list.add(new DecNumber(buffer, offset + 4, 4, "Coordinate: Y"));
    list.add(new TextString(buffer, offset + 8, 500, "Text"));
    list.add(new Bitmap(buffer, offset + 508, 4, "Is read only?", s_yesno));
    list.add(new Unknown(buffer, offset + 512, 20));
    return offset + 532;
  }
}

