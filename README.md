## Xml Tree
### _Lightweight cross-platform XML files structure viewer_

[![Java](https://img.shields.io/badge/Java-8+-green)](https://www.java.com/) [![Swing](https://img.shields.io/badge/GUI-Swing-orange)](https://en.wikipedia.org/wiki/Swing_(Java)) [![Maven](https://img.shields.io/badge/build-Maven-blue)](https://maven.apache.org/) [![Version](https://img.shields.io/badge/version-3.7-lightgrey)](#)

🇺🇦 [Українська версія](README_ua.md)

### 💡 Purpose
Make a lightweight multiplatform GUI application with native Java only for convenient viewing of the structure of XML files: the file is shown as a tree, the selected tag's name, value and attributes are highlighted, and its content is shown formatted (AKA Fine Print), ready to be copied.

### 📃 Features
- [Main window](#MainWin) shows XML file structure as a tree with automatically formatted XML of the selected tag
- [Copying](#Copying) of Tag and Attribute Names and Values in several formats
- [Fuzzy search](#Search) in Tag names / values and Attribute names / values
- [Multiple windows](#AppMenu) – every file is opened in its own independent window
- [Themes](#AppMenu) – standard Swing Look and Feel switching for all opened windows
- [Opening a file from the command line](#Launch)

### ⭐ Advantages
- **Lightweight** – less than 100Kb jar file without any third-party libraries, quick start.
- **Multiplatform** – runs anywhere with Java 8 or newer (Linux, Windows, macOS).
- **Safe for untrusted files** – external DTDs and external entities are never loaded, so opening a file makes no network or local file requests.
- **Encoding aware** – the file encoding is taken from the XML declaration or BOM (e.g. `windows-1251` files are shown correctly).
- **Easy to use** – standard menus, hotkeys and popup menu.

### 📱 Screenshots
<table>
  <tr>
    <td align="center"><img src="images/xmltree_main.png" width="600" alt="Main window" /></td>
  </tr>
  <tr>
    <td align="center"><i>Main window</i></td>
  </tr>
</table>

### 📌 Using

#### Launch<a id='Launch' />
**_Run the jar file with JRE 8 or newer, optionally passing the XML file to show._**
- Linux: `launch.sh [file]` or `java -jar xmltree.jar [file]`
- Windows: `launch.cmd [file]` or `javaw -jar xmltree.jar [file]`
- Without a parameter (or if the file doesn't exist) the standard Open File Dialog appears in the current directory; only `*.xml` and `*.xsd` files are listed.
- If the file is not well-formed XML, an error message is shown and the window stays empty – press **Open** to select another file.

#### Main Window<a id='MainWin' />
**_Tree of tags on the left, selected tag details on the right (see the screenshot)._**
1. XML file tree view with the currently selected Tag
2. the Tag and Attribute(s) view with values, colour highlighted: tag name (blue), attribute names (red), values (green)
3. formatted XML view of the selected Tag
4. Tag and Attribute(s) view's popup Menu (see [Copying](#Copying))
5. Search panel (see [Search](#Search))

#### Copying<a id='Copying' />
**_Right click on the Tag and Attribute(s) view opens the popup menu; the result goes to the system clipboard._**
- **Copy tag name** – Name of the Tag (blue)
- **Copy attribute(s) Name(s)** – **All** Attribute names of the current Tag (red), one per line
- **Copy attribute(s) Value(s)** – **All** Attribute values of the current Tag (green), one per line
- **Copy All as 'key=value'** – the Tag and **All** its Attributes as **key=value** lines
- **Copy All as XML** – the formatted XML view's content

The attribute items are shown only when the Tag has attributes.

#### Search<a id='Search' />
**_Case-insensitive fuzzy search through the whole tree._**
1. Open the Search panel: **Search → Find in...** (`Ctrl+F`).
2. Type the text and select where to search: **Tag names**, **Tag values**, **Attr names**, **Attr values** or **Everywhere**.
3. Press **Search** or `Enter`. A Tag matches if it contains the text or differs from it by no more than 30% of the text length (Levenshtein distance).
4. Navigate with **<<** / **>>** or `Shift+F3` / `F3`; the search starts from the selected Tag and wraps around. **Position: N of M** shows the current result.
5. `Esc` hides the Search panel.

#### Application Menu<a id='AppMenu' />
**_All actions are available from the menu and hotkeys._**
- **File**
  - **Open** (`Ctrl+O`) – open an XML file in a new window
  - **Exit** (`Alt+F4`) – close all opened windows and exit the application
- **Search**
  - **Find in...** (`Ctrl+F`) – show or hide the Search panel (also hidden by `Esc`)
  - **Find next** (`F3`) – show the next search result
  - **Find prev** (`Shift+F3`) – show the previous search result
- **Windows**
  - **Close this** (`Ctrl+F4`) – close the active window
  - **Close All** (`Alt+F4`) – close all opened windows and exit the application (same as **File → Exit**)
  - **Xml Tree: <file_name>** (`Ctrl+0` … `Ctrl+8`) – switch to another opened window
- **Theme** – select the Theme (Look and Feel) for all opened windows

### 🚀 Building the application
Download the zip file from the [Releases](https://github.com/anrydas/xmltree/releases) section, unzip and [launch it](#Launch).

Or build it from sources. Requirements: JDK 8 or newer (Maven is provided by the wrapper).
```shell
git clone https://github.com/anrydas/xmltree.git
cd xmltree
./mvnw clean package     # build, run tests; Windows: mvnw.cmd clean package
./mvnw test              # run tests only
```
The build output appears in `target/`: `xmltree-<version>.jar` and the distribution archive `xmltree_v.<version>-distrib.zip` (jar, launch scripts, icons).

**Note:** If you are using Windows with a display scale greater than 100%, launching the application with JRE 8 makes the interface look better (smaller controls) than with newer JREs.

###### _Made by -=:dAs:=-_
