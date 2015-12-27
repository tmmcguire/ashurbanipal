BEGIN {
  FS = "\t";
}

($2 == "positive") && ($3 == "1") {
  print $1"\t1"
}

($2 == "negative") && ($3 == "1") {
  print $1"\t-1"
}
