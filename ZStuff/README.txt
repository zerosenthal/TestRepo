The primary test code is commented thoroughly to correspond with which part of the req's it fulfills.
Those files are BTreeImplTest DocumentIOImplTest DocumentStoreImplTest
I also included lightly re-purposed Shalom Gottesman test code, for emphasis that my code works and to catch more edge cases, since he tested in, uh, different ways than my more direct tests.

Assorted facts/assumptions:
Primary memManage is done by DSI. This is done by reliance on protected getArrayIndex() in MinHeapImpl to check if a bTree.get() produced a document that was just pulled from disc. More elegant and more annoying to implement solutions were considered.
If a doc is moved to disc (by bTree or CommandStack) it is only returned to memory if actively summoned with a bTree.get or a DSI.undo().
You cannot have two different DocumentStoreImpl's running simultaneously since their Undo folders are the same and will overwrite eachother.
The Trie stores URIWrappers (not Documents) and the Command Stack stores URIs (not Documents).
Directories and files from bTree.moveToDisc() will not be cleaned up after running if they were moved to disc (and not gotten back from disc) during runtime. Though, the old files cannot be accessed except if they are put() in the new BTree, and any moveToDisk of the exact same file name will simply overrwrite it. Therefore, no "echoes" can be heard of these old files.
The CommandStack directory is cleared out at the beginning of Runtime, to prevent URI overlap issues. This is simpler because I decided what directory to put the CommandStack Files into.
The Class that does the serialization is called GSONDocUtility. DocumentIOImpl handles the File and Path stuff.
DocumentStoreImpl deletion or overwriting will produce serialized docs stored to disc because of the CommandStack
The MinHeapImpl and TrieImpl class have no "bias" towards the Document class, while BTreeImpl has DocumentIOImpl hardcoded into the get() from disc and the moveToDisc() methods.



