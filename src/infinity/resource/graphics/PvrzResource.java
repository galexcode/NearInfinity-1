// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.resource.graphics;

import infinity.gui.ButtonPopupMenu;
import infinity.gui.RenderCanvas;
import infinity.gui.WindowBlocker;
import infinity.icon.Icons;
import infinity.resource.Closeable;
import infinity.resource.Resource;
import infinity.resource.ResourceFactory;
import infinity.resource.ViewableContainer;
import infinity.resource.key.ResourceEntry;
import infinity.util.DynamicArray;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class PvrzResource implements Resource, ActionListener, Closeable
{
  private final ResourceEntry entry;
  private ButtonPopupMenu mnuExport;
  private JMenuItem miExport, miPNG;
  private RenderCanvas rcImage;
  private JPanel panel;

  public PvrzResource(ResourceEntry entry) throws Exception
  {
    this.entry = entry;
  }

//--------------------- Begin Interface ActionListener ---------------------

  @Override
  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() == miExport) {
      // export as original PVRZ
      ResourceFactory.getInstance().exportResource(entry, panel.getTopLevelAncestor());
    } else if (event.getSource() == miPNG) {
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String fileName = entry.toString().replace(".PVRZ", ".PNG");
        BufferedImage image = getImage();
        if (ImageIO.write(image, "png", os)) {
          ResourceFactory.getInstance().exportResource(entry, os.toByteArray(),
                                                       fileName, panel.getTopLevelAncestor());
        } else {
          JOptionPane.showMessageDialog(panel.getTopLevelAncestor(),
                                        "Error while exporting " + entry, "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        os.close();
        os = null;
        image = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

//--------------------- End Interface ActionListener ---------------------

//--------------------- Begin Interface Resource ---------------------

  @Override
  public ResourceEntry getResourceEntry()
  {
    return entry;
  }

//--------------------- End Interface Resource ---------------------

//--------------------- Begin Interface Closeable ---------------------

  @Override
  public void close() throws Exception
  {
    panel.removeAll();
    rcImage.setImage(null);
    rcImage = null;
  }

//--------------------- End Interface Closeable ---------------------

//--------------------- Begin Interface Viewable ---------------------

  @Override
  public JComponent makeViewer(ViewableContainer container)
  {
    miExport = new JMenuItem("original");
    miExport.addActionListener(this);
    miPNG = new JMenuItem("as PNG");
    miPNG.addActionListener(this);
    mnuExport = new ButtonPopupMenu("Export...", new JMenuItem[]{miExport, miPNG});
    mnuExport.setIcon(Icons.getIcon("Export16.gif"));
    mnuExport.setMnemonic('e');
    rcImage = new RenderCanvas();
    rcImage.setHorizontalAlignment(SwingConstants.CENTER);
    rcImage.setVerticalAlignment(SwingConstants.CENTER);
    WindowBlocker.blockWindow(true);
    try {
      rcImage.setImage(loadImage());
      WindowBlocker.blockWindow(false);
    } catch (Exception e) {
      WindowBlocker.blockWindow(false);
    }
    JScrollPane scroll = new JScrollPane(rcImage);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    scroll.getHorizontalScrollBar().setUnitIncrement(16);

    JPanel bPanel = new JPanel();
    bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    bPanel.add(mnuExport);

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(scroll, BorderLayout.CENTER);
    panel.add(bPanel, BorderLayout.SOUTH);
    scroll.setBorder(BorderFactory.createLoweredBevelBorder());

    return panel;
  }

//--------------------- End Interface Viewable ---------------------

  public BufferedImage getImage()
  {
    if (rcImage != null) {
      return ColorConvert.toBufferedImage(rcImage.getImage(), false);
    } else if (entry != null) {
      return loadImage();
    }
    return null;
  }

  private BufferedImage loadImage()
  {
    BufferedImage image = null;
    PvrDecoder decoder = null;
    if (entry != null) {
      try {
        byte[] data = entry.getResourceData();
        int size = DynamicArray.getInt(data, 0);
        int marker = DynamicArray.getUnsignedShort(data, 4);
        if ((size & 0xff) != 0x34 && marker != 0x9c78)
          throw new Exception("Invalid PVRZ resource");
        data = Compressor.decompress(data, 0);

        decoder = new PvrDecoder(data);
        image = ColorConvert.createCompatibleImage(decoder.info().width(),
                                                   decoder.info().height(), true);
        if (!decoder.decode(image)) {
          image = null;
        }
        decoder.close();
      } catch (Exception e) {
        image = null;
        if (decoder != null) {
          decoder.close();
          decoder = null;
        }
        e.printStackTrace();
      }
    }
    return image;
  }

}
