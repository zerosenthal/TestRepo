Handled "doc graveyard" by creating a GraveyardDocIO class. Borrows DocIO serialize/deserialize logic,
but stores deleted docs in "graveyard" dir generated in baseDir, and tracks multiple doc versions for undo/redo purposes

Handled interface between bTree and DSI memory management by creating wrapper method DSI.get(uri). Any time DSI would
call bTree.get(uri), calls wrapper class instead. Checks if doc is in MemManagement MinHeap - if it is, must be in memory,
and proceeds accordingly. If not, updates MemManagement assuming doc is being brought back from disk.

Every time a doc is sent to disk by MemManagement, uri is added to a stack. If a put is ever undone, DSI will bring
docs back into memory based on LIFO order of uri's in the stack.

As frustrating as this project has been at times - this stage in particular - thank you for putting up with my incessant
demands for clarification and challenging me to think outside the box. I really enjoyed this semester, and look forward
to the coming years in the CS department. Thank you, and have a great summer!

Thanks,
Elimelekh