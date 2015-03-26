BEGIN {
  FS="\t"
}

{
  for (i = 2; i < NF; ++i) {
    split($i, a, " ");
    gsub("'", "''", a[1]);
    printf "insert into topics (etext_no,word,count) values (%d, (select id from nouns where word = '%s'), %d);\n", $1, a[1], a[2];
  }
}
