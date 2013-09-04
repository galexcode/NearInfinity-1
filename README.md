# Near Infinity #

A file browser and editor for the Infinity Engine.

## Contributors ##

This section contains information intended for those who contribute
code to Near Infinity (NI). It contains some information on style and
explains how the code is organised and added to.

### Style ###

Code style is intended to improve code readability and to reduce
"diff noise" (meaningless changes).

Simply put, match the existing style. In particular:

* Use an indentation level of 2 spaces, not tab stops.

* Pay attention to how the existing code is indented and try to adhere
  to the same pattern.

* Do not leave trailing white space. Most decent editors have tools to
  help with this.

* End files with a single newline. Again, decent editors...

Additionally, try to avoid overly long lines. Breaking lines at column
80 is very standard, but since this is ReallyRidiculouslyLongNames
Java it's not always practical. Do try to limit yourself to less than
column 100, however, or it becomes difficult to read the code on
GitHub. The existing code is not always well-behaved in this regard,
however.

When in doubt, refer to the official
[Java conventions](http://www.oracle.com/technetwork/java/codeconv-138413.html).

### Workflow ###

There are 2 sets of 2 persistent branches. Of the first set:

* master - code that reflects the latest stable release.

* devel - code that is ready to go into the next unstable release with
  little to no adjustment. That is, it should be complete and behave
  well locally, even if it has not been exhaustively tested under a
  wide variety of conditions.

If you work on something you should generally do so on a feature
branch based off devel. Once the feature is complete, tested and
preferably reviewed, it can be merged back into devel. While you are
working on your branch, be sure to keep it up to date with devel, in
order to avoid large divergences (and the resulting messy
merges). Small, straightforward commits can be made directly to
devel. Working off a feature branch also has the advantage of letting
others easily check out your work (provided you push it, naturally),
since they are able to pull your changes and simply check out your
branch (compared to needing to merge your devel branch into their own,
or clone a new local repository for your code).

The second set are the branches ci and devel-ci, which correspond
directly to master and devel except they contain additional patches
for allowing NI to run on a lower-cased game in a case-sensitive
environment. Consequently, the only things that should be committed
to devel-ci are changes that are about case-sensitive IO. Regular
changes should be committed to devel, which is then merged into
devel-ci.
