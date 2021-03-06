// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.resource.nwn.gff;

import infinity.resource.Resource;
import infinity.resource.ViewableContainer;
import infinity.resource.Writeable;
import infinity.resource.key.FileResourceEntry;
import infinity.resource.key.ResourceEntry;
import infinity.resource.nwn.gff.field.GffByte;
import infinity.resource.nwn.gff.field.GffChar;
import infinity.resource.nwn.gff.field.GffDouble;
import infinity.resource.nwn.gff.field.GffDword;
import infinity.resource.nwn.gff.field.GffDword64;
import infinity.resource.nwn.gff.field.GffExoLocString;
import infinity.resource.nwn.gff.field.GffExoString;
import infinity.resource.nwn.gff.field.GffField;
import infinity.resource.nwn.gff.field.GffFloat;
import infinity.resource.nwn.gff.field.GffInt;
import infinity.resource.nwn.gff.field.GffInt64;
import infinity.resource.nwn.gff.field.GffList;
import infinity.resource.nwn.gff.field.GffResRef;
import infinity.resource.nwn.gff.field.GffShort;
import infinity.resource.nwn.gff.field.GffStruct;
import infinity.resource.nwn.gff.field.GffVoid;
import infinity.resource.nwn.gff.field.GffWord;
import infinity.util.DynamicArray;
import infinity.util.Filewriter;
import infinity.util.StringResource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

@Deprecated
public class GffResource implements Resource, Writeable
{
  private final GffStruct topStruct;
  private final ResourceEntry entry;
  private final String fileType, fileVersion;

  public static void main(String args[]) throws Exception
  {
    StringResource.init(new File(args[0]));
    ResourceEntry file1 = new FileResourceEntry(new File(args[1]));
    ResourceEntry file2 = new FileResourceEntry(new File(args[2]));
    GffResource res1 = new GffResource(file1);
    GffResource res2 = new GffResource(file2);
    res1.topStruct.compare(res2.topStruct);
  }

  public GffResource(ResourceEntry entry) throws Exception
  {
    this.entry = entry;
    byte buffer[] = entry.getResourceData();

    // Header
    fileType = new String(buffer, 0, 4);
    fileVersion = new String(buffer, 4, 4);
    if (!fileVersion.equalsIgnoreCase("V3.2"))
      throw new Exception("Unsupported GFF version: " + fileVersion);
    int structOffset = DynamicArray.getInt(buffer, 8);
    int structCount = DynamicArray.getInt(buffer, 12);
    int fieldOffset = DynamicArray.getInt(buffer, 16);
    int fieldCount = DynamicArray.getInt(buffer, 20);
    int labelOffset = DynamicArray.getInt(buffer, 24);
    int labelCount = DynamicArray.getInt(buffer, 28);
    int fieldDataOffset = DynamicArray.getInt(buffer, 32);
    int fieldDataCount = DynamicArray.getInt(buffer, 36);
    int fieldIndicesOffset = DynamicArray.getInt(buffer, 40);
    int fieldIndicesCount = DynamicArray.getInt(buffer, 44);
    int listIndicesOffset = DynamicArray.getInt(buffer, 48);
    int listIndicesCount = DynamicArray.getInt(buffer, 52);

//    System.out.println(structOffset + " - " + structCount);
//    System.out.println(fieldOffset + " - " + fieldCount);
//    System.out.println(labelOffset + " - " + labelCount);
//    System.out.println(fieldDataOffset + " - " + fieldDataCount);
//    System.out.println(fieldIndicesOffset + " - " + fieldIndicesCount);
//    System.out.println(listIndicesOffset + " - " + listIndicesCount);

    GffStruct structs[] = new GffStruct[structCount];
    for (int i = 0; i < structCount; i++)
      structs[i] = new GffStruct(buffer, structOffset + 12 * i);
    topStruct = structs[0];

    List<GffList> lists = new ArrayList<GffList>();
    GffField fields[] = new GffField[fieldCount];

    for (int i = 0; i < fieldCount; i++) {
      int offset = fieldOffset + i * 12;
      int type = DynamicArray.getInt(buffer, offset);
      switch (type) {
        case 0:
          fields[i] = new GffByte(buffer, offset, labelOffset);
          break;
        case 1:
          fields[i] = new GffChar(buffer, offset, labelOffset);
          break;
        case 2:
          fields[i] = new GffWord(buffer, offset, labelOffset);
          break;
        case 3:
          fields[i] = new GffShort(buffer, offset, labelOffset);
          break;
        case 4:
          fields[i] = new GffDword(buffer, offset, labelOffset);
          break;
        case 5:
          fields[i] = new GffInt(buffer, offset, labelOffset);
          break;
        case 6:
          fields[i] = new GffDword64(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 7:
          fields[i] = new GffInt64(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 8:
          fields[i] = new GffFloat(buffer, offset, labelOffset);
          break;
        case 9:
          fields[i] = new GffDouble(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 10:
          fields[i] = new GffExoString(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 11:
          fields[i] = new GffResRef(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 12:
          fields[i] = new GffExoLocString(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 13:
          fields[i] = new GffVoid(buffer, offset, labelOffset, fieldDataOffset);
          break;
        case 14:
          int labelIndex = DynamicArray.getInt(buffer, offset + 4);
          int structArrayOffset = DynamicArray.getInt(buffer, offset + 8);
          String label = DynamicArray.getString(buffer, labelOffset + labelIndex * 16, 16);
          fields[i] = structs[structArrayOffset];
          fields[i].setLabel(label);
          break;
        case 15:
          fields[i] = new GffList(buffer, offset, labelOffset, listIndicesOffset);
          lists.add((GffList)fields[i]);
          break;
        default:
          System.err.println("Unknown type: " + type);
      }
    }

    for (final GffStruct struct : structs)
      struct.initFields(fields, buffer, fieldIndicesOffset);

    for (int i = 0; i < lists.size(); i++)
      lists.get(i).initStructs(structs);
  }

// --------------------- Begin Interface Resource ---------------------

  @Override
  public ResourceEntry getResourceEntry()
  {
    return entry;
  }

// --------------------- End Interface Resource ---------------------


// --------------------- Begin Interface Viewable ---------------------

  @Override
  public JComponent makeViewer(ViewableContainer container)
  {
    return new DefaultGffEditor(this);
  }

// --------------------- End Interface Viewable ---------------------


// --------------------- Begin Interface Writeable ---------------------

  @Override
  public void write(OutputStream os) throws IOException
  {
    List<GffStruct> structs = new ArrayList<GffStruct>();
    List<GffField> fields = new ArrayList<GffField>();
    List<GffList> lists = new ArrayList<GffList>();
    structs.add(topStruct);
    topStruct.addNestedFields(structs, fields, lists);
    Set<String> labelSet = new HashSet<String>();
    for (int i = 0; i < fields.size(); i++)
      labelSet.add(fields.get(i).getLabel());
    List<String> labels = new ArrayList<String>(labelSet);

    int structOffset = 56;
    int structCount = structs.size();
    int fieldOffset = structOffset + 12 * structCount;
    int fieldCount = fields.size();
    int labelOffset = fieldOffset + 12 * fieldCount;
    int labelCount = labels.size();
    int fieldDataOffset = labelOffset + 16 * labelCount;
    int fieldDataCount = 0;
    for (int i = 0; i < fields.size(); i++)
      fieldDataCount += fields.get(i).getFieldDataSize();
    int fieldIndicesOffset = fieldDataOffset + fieldDataCount;
    int fieldIndicesCount = 0;
    for (int i = 0; i < structs.size(); i++)
      fieldIndicesCount += structs.get(i).getFieldIndicesSize();
    int listIndicesOffset = fieldIndicesOffset + fieldIndicesCount;
    int listIndicesCount = 0;
    for (int i = 0; i < lists.size(); i++)
      listIndicesCount += lists.get(i).getListIndiciesSize();

    // Write header
    Filewriter.writeString(os, fileType, 4);
    Filewriter.writeString(os, fileVersion, 4);
    Filewriter.writeInt(os, structOffset);
    Filewriter.writeInt(os, structCount);
    Filewriter.writeInt(os, fieldOffset);
    Filewriter.writeInt(os, fieldCount);
    Filewriter.writeInt(os, labelOffset);
    Filewriter.writeInt(os, labelCount);
    Filewriter.writeInt(os, fieldDataOffset);
    Filewriter.writeInt(os, fieldDataCount);
    Filewriter.writeInt(os, fieldIndicesOffset);
    Filewriter.writeInt(os, fieldIndicesCount);
    Filewriter.writeInt(os, listIndicesOffset);
    Filewriter.writeInt(os, listIndicesCount);

    // Write struct array & prepare field indices array
    int fieldIndices[] = new int[fieldIndicesCount / 4];
    int fieldIndicesIndex = 0;
    for (int i = 0; i < structs.size(); i++)
      fieldIndicesIndex = structs.get(i).writeStruct(os, fields, fieldIndices, fieldIndicesIndex);

    // Write field array & prepare field data block & list indices array
    byte fieldData[] = new byte[fieldDataCount];
    int listIndices[] = new int[listIndicesCount / 4];
    int fieldDataIndex = 0, listIndicesIndex = 0;
    for (int i = 0; i < fields.size(); i++) {
      GffField field = fields.get(i);
      if (field instanceof GffList)
        listIndicesIndex = ((GffList)field).writeField(os, labels, structs, listIndices, listIndicesIndex);
      else if (field instanceof GffStruct)
        ((GffStruct)field).writeField(os, labels, structs);
      else
        fieldDataIndex = field.writeField(os, labels, fieldData, fieldDataIndex);
    }

    // Write label array
    for (int i = 0; i < labels.size(); i++)
      Filewriter.writeString(os, labels.get(i), 16);

    // Write field data block
    Filewriter.writeBytes(os, fieldData);

    // Write field indices array
    for (final int fieldIndex : fieldIndices)
      Filewriter.writeInt(os, fieldIndex);

    // Write list indices array
    for (final int listIndex : listIndices)
      Filewriter.writeInt(os, listIndex);
  }

// --------------------- End Interface Writeable ---------------------

  protected GffStruct getTopStruct()
  {
    return topStruct;
  }
}

