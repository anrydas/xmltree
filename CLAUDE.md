# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Xml Tree – a Java Swing desktop viewer for XML file structure. No runtime dependencies beyond the JDK; the jar must stay small (< 100Kb) – do not add runtime libraries. Source/target is **Java 8**: don't use newer APIs (`Path.of`, `var`, `List.of`, …), also in tests. The build JDK here is 21 without `ct.sym`, so `maven.compiler.release=8` does not work – keep `source`/`target`.

## Commands

```shell
./mvnw clean package                                    # compile, test, build target/xmltree-<ver>.jar and target/xmltree_v.<ver>-distrib.zip
./mvnw test                                             # all tests (JUnit 5)
./mvnw test -Dtest=XmlFileTest                          # one class
./mvnw test -Dtest=SearchProcessorTest#levenshtein      # one method
java -jar target/xmltree-<ver>.jar path/to/file.xml     # run
```

`javac` is not on PATH; Maven compiles in-process. For a quick throwaway harness use the source launcher: `java -cp target/classes Foo.java`. `temp/` holds local sample XML files (untracked). The distribution zip content is defined in `distribution/assembly.xml`.

## Architecture

- `Main` → `SwingUtilities.invokeLater(new XmlForm(file, true))`. **One `XmlForm` per window/file**, created and used on the EDT only; opening another file creates another `XmlForm`. The start window asks for a file if none was passed.
- Application-wide state lives in two small static holders: `AppWindows` (registry of open frames + change listeners; each form rebuilds its **Windows** menu on change so its hotkeys work without opening the menu) and `XmlFileChooser` (one shared `JFileChooser` that remembers the last directory).
- `XmlForm` is the window controller: frame/menus, tree loading, selection display, clipboard commands. It wires `GuiView` (pure component construction + getters), `AppMenus` (File/Search/Windows/Theme menus and the attribute popup) and `AppActions` (one `AbstractAction` subclass per command in `menu/action/`, each just calls a method on `XmlForm`). New commands: action class → field/getter in `AppActions` → menu item in `AppMenus` → method on `XmlForm`.
- `search/SearchController` owns the search panel of a window (run, progress, next/prev); `XmlForm.findIn/findForward/findBackward` delegate to it. `format/TagFormatter` holds the pure text output (info-pane HTML with escaping, clipboard formats).
- `entity/XmlFile` parses with a hardened DOM parser (no external DTD/entities, encoding from declaration/BOM, non-deferred DOM) and strips formatting whitespace; parse failures throw `XmlFileException` and the form falls back to `XmlFile.noFile()` (root tag `no_file`). Missing file → placeholder, no exception.
- Tree nodes are `DefaultMutableTreeNode`s whose user object is `XmlTagInfo` (name, value, attributes eagerly; the formatted XML of the subtree **lazily** via `getText()`, cached, serialized under a global lock because DOM isn't thread-safe). The top node's user object is a `String` (file name) – code that casts user objects must skip it.
- All tree/DOM walks are iterative (explicit stack or `preorderEnumeration`) so very deep documents don't overflow the stack – keep it that way.
- Threading: `form/LoadTreeDataTask` and `search/SearchTask` are `SwingWorker`s. Background code must not touch Swing components or the live tree model – build detached data, report progress via `publish`/`process`, apply results in `done()` (which calls `get()` so errors are shown, not swallowed).
- Search (`search/SearchProcessor`) walks the tree in document order and matches case-insensitive substring or Levenshtein distance ≤ 30% of the query length. Results at/after the selected node decide where navigation starts; `setCurrentSearchResultValue()` returns `false` when none – callers then wrap with `findForward()`.

## Docs

README is bilingual: `README.md` + `README_ua.md` – update both together (use the `readme` skill).
