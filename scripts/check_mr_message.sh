SB=$CI_MERGE_REQUEST_SOURCE_BRANCH_NAME
TB=$CI_MERGE_REQUEST_TARGET_BRANCH_NAME
git checkout $SB
git checkout $TB
echo Branches are: $SB/$TB >&2
SC=$(git log -n 1 --pretty=format:"%H" $SB --)
TC=$(git log -n 1 --pretty=format:"%H" $TB --)
echo Commits are $SC/$TC >&2
MERGE_BASE=$(git merge-base $SC $TC)
echo MergeBase: $MERGE_BASE >&2
CMTS="$(git log $TC...$SC --format="%H %s")"
INVALID_COMMITS="$(echo "$CMTS" | grep -vE '^[0-9a-f]+ ((Requirement|Task|Bug|Ticket|Feedback|Subtask)-[0-9]+: |Merge (remote-tracking )?(branch|merge)|(M|m)erge: merge)')"
echo MR labels are: "$CI_MERGE_REQUEST_LABELS"
if [[ ",$CI_MERGE_REQUEST_LABELS," =~ [.*,Misc,.*] ]]; then
  INVALID_COMMITS="$(echo "$INVALID_COMMITS" | grep -vE '^[0-9a-f]+ Misc: ')"
fi
if [[ $INVALID_COMMITS != "" ]]; then
  echo "Invalid commits:"
  echo "$INVALID_COMMITS"
  exit 1
fi
exit 0