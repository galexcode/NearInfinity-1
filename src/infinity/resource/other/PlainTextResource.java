// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.resource.other;

import infinity.gui.BrowserMenuBar;
import infinity.gui.ButtonPopupMenu;
import infinity.icon.Icons;
import infinity.resource.Closeable;
import infinity.resource.ResourceFactory;
import infinity.resource.TextResource;
import infinity.resource.ViewableContainer;
import infinity.resource.Writeable;
import infinity.resource.key.BIFFResourceEntry;
import infinity.resource.key.ResourceEntry;
import infinity.search.TextResourceSearcher;
import infinity.util.Decryptor;
import infinity.util.Filewriter;
import infinity.util.NIFile;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class PlainTextResource implements TextResource, Writeable, ActionListener, ItemListener,
                                                DocumentListener, Closeable
{
  private final ResourceEntry entry;
  private final String text;
  private ButtonPopupMenu bfind;
  private JButton bsave, bexport, bremoveSpaces;
  private JMenuItem ifindall, ifindthis;
  private JPanel panel;
  private JTextArea editor;
  private boolean resourceChanged;

  public PlainTextResource(ResourceEntry entry) throws Exception
  {
    this.entry = entry;
    byte data[] = entry.getResourceData();
    if (data != null && data.length > 1 && data[0] == -1)
      text = Decryptor.decrypt(data, 2, data.length);
    else
      text = new String(data);
  }

// --------------------- Begin Interface ActionListener ---------------------

  @Override
  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() == bsave) {
      if (ResourceFactory.getInstance().saveResource(this, panel.getTopLevelAncestor()))
        resourceChanged = false;
    }
    else if (event.getSource() == bexport)
      ResourceFactory.getInstance().exportResource(entry, panel.getTopLevelAncestor());
    else if (event.getSource() == bremoveSpaces) {
      StringBuffer newText = new StringBuffer(editor.getText().length());
      StringTokenizer st = new StringTokenizer(editor.getText(), "\n");
      while (st.hasMoreTokens())
        newText.append(st.nextToken().trim()).append('\n');
      editor.setText(newText.toString());
      editor.setCaretPosition(0);
    }
  }

// --------------------- End Interface ActionListener ---------------------


// --------------------- Begin Interface Closeable ---------------------

  @Override
  public void close() throws Exception
  {
    if (resourceChanged) {
      File output;
      if (entry instanceof BIFFResourceEntry)
        output =
            NIFile.getFile(ResourceFactory.getRootDirs(),
                 ResourceFactory.OVERRIDEFOLDER + File.separatorChar + entry.toString());
      else
        output = entry.getActualFile();
      String options[] = {"Save changes", "Discard changes", "Cancel"};
      int result = JOptionPane.showOptionDialog(panel, "Save changes to " + output + '?', "Resource changed",
                                                JOptionPane.YES_NO_CANCEL_OPTION,
                                                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
      if (result == 0)
        ResourceFactory.getInstance().saveResource(this, panel.getTopLevelAncestor());
      else if (result == 2)
        throw new Exception("Save aborted");
    }
  }

// --------------------- End Interface Closeable ---------------------


// --------------------- Begin Interface DocumentListener ---------------------

  @Override
  public void insertUpdate(DocumentEvent event)
  {
    resourceChanged = true;
  }

  @Override
  public void removeUpdate(DocumentEvent event)
  {
    resourceChanged = true;
  }

  @Override
  public void changedUpdate(DocumentEvent event)
  {
    resourceChanged = true;
  }

// --------------------- End Interface DocumentListener ---------------------


// --------------------- Begin Interface ItemListener ---------------------

  @Override
  public void itemStateChanged(ItemEvent event)
  {
    if (event.getSource() == bfind) {
      if (bfind.getSelectedItem() == ifindall) {
        String type = entry.toString().substring(entry.toString().indexOf(".") + 1);
        List<ResourceEntry> files = ResourceFactory.getInstance().getResources(type);
        new TextResourceSearcher(files, panel.getTopLevelAncestor());
      }
      else if (bfind.getSelectedItem() == ifindthis) {
        List<ResourceEntry> files = new ArrayList<ResourceEntry>();
        files.add(entry);
        new TextResourceSearcher(files, panel.getTopLevelAncestor());
      }
    }
  }

// --------------------- End Interface ItemListener ---------------------


// --------------------- Begin Interface Resource ---------------------

  @Override
  public ResourceEntry getResourceEntry()
  {
    return entry;
  }

// --------------------- End Interface Resource ---------------------


// --------------------- Begin Interface TextResource ---------------------

  @Override
  public String getText()
  {
    return text;
  }

  @Override
  public void highlightText(int linenr, String text)
  {
    String s = editor.getText();
    int startpos = 0;
    for (int i = 1; i < linenr; i++)
      startpos = s.indexOf("\n", startpos + 1);
    if (startpos == -1) return;
    int wordpos = s.toUpperCase().indexOf(text.toUpperCase(), startpos);
    if (wordpos != -1)
      editor.select(wordpos, wordpos + text.length());
    else
      editor.select(startpos, s.indexOf("\n", startpos + 1));
    editor.getCaret().setSelectionVisible(true);
  }

// --------------------- End Interface TextResource ---------------------


// --------------------- Begin Interface Viewable ---------------------

  @Override
  public JComponent makeViewer(ViewableContainer container)
  {
    editor = new JTextArea();
    editor.addCaretListener(container.getStatusBar());
    editor.setText(text);
    editor.setFont(BrowserMenuBar.getInstance().getScriptFont());
    editor.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    editor.setCaretPosition(0);
    editor.setLineWrap(false);
    editor.getDocument().addDocumentListener(this);
    if (entry.toString().toUpperCase().endsWith(".BIO") || entry.toString().toUpperCase().endsWith(".RES")) {
      editor.setLineWrap(true);
      editor.setWrapStyleWord(true);
    }
    bexport = new JButton("Export...", Icons.getIcon("Export16.gif"));
    bexport.setMnemonic('e');
    bexport.setToolTipText("NB! Will export last *saved* version");
    bexport.addActionListener(this);
    bsave = new JButton("Save", Icons.getIcon("Save16.gif"));
    bsave.setMnemonic('a');
    bsave.addActionListener(this);
    bremoveSpaces = new JButton("Trim spaces", Icons.getIcon("Refresh16.gif"));
    bremoveSpaces.addActionListener(this);

    ifindall =
    new JMenuItem("in all " + entry.toString().substring(entry.toString().indexOf(".") + 1) + " files");
    ifindthis = new JMenuItem("in this file only");
    bfind = new ButtonPopupMenu("Find...", new JMenuItem[]{ifindall, ifindthis});
    bfind.addItemListener(this);
    bfind.setIcon(Icons.getIcon("Find16.gif"));

    JPanel bpanel = new JPanel();
    bpanel.setLayout(new GridLayout(1, 4, 6, 0));
    bpanel.add(bfind);
    bpanel.add(bremoveSpaces);
    bpanel.add(bexport);
    bpanel.add(bsave);

    JPanel lowerpanel = new JPanel();
    lowerpanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    lowerpanel.add(bpanel);

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(new JScrollPane(editor), BorderLayout.CENTER);
    panel.add(lowerpanel, BorderLayout.SOUTH);

    return panel;
  }

// --------------------- End Interface Viewable ---------------------


// --------------------- Begin Interface Writeable ---------------------

  @Override
  public void write(OutputStream os) throws IOException
  {
    if (editor == null)
      Filewriter.writeString(os, text, text.length());
    else {
      String s = editor.getText();
      int index = s.indexOf((int)'\n');
      while (index != -1) {
        s = s.substring(0, index) + '\r' + s.substring(index);
        index = s.indexOf((int)'\n', index + 2);
      }
      Filewriter.writeString(os, s, s.length());
    }
  }

// --------------------- End Interface Writeable ---------------------

  public List<String> extract2DAHeaders()
  {
    StringTokenizer st = new StringTokenizer(getText(), "\n");
    st.nextToken();
    st.nextToken();
    String header = st.nextToken();
    st = new StringTokenizer(header);
    List<String> strings = new ArrayList<String>();
    while (st.hasMoreTokens())
      strings.add(st.nextToken().toUpperCase());
    return strings;
  }
}

