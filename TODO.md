# TODO

- [ ] Handle concurrent agent instances safely: running multiple applications with JCT in parallel can cause trouble when they write to the same log and output files (for example in shared `-Djct.logDir` or shared `processor.stackFolderName`).

