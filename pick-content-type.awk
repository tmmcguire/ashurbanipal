# pick-content-type.awk: Choose the best of multiple formats for processing.

# This AWK program reads a to-do list file containing information
# about many texts, each of which may be in many formats. It ignores
# non-plain-text formats and chooses the "best" format option if there
# are multiple options.

# To-do list format:
# etext_no	language	content_type	filename

function score(type) {
    for (i = 0; i < 21; ++i) {
        if (type == scores[i]) {
            return i;
        }
    }
}

BEGIN {
    FS = "\t"
    pending = "";
    # Possible content-types, by preference:
    scores[0] = "text/plain; charset=\"utf-8\"";
    scores[1] = "text/plain; charset=\"iso-8859-1\"";
    scores[2] = "text/plain; charset=\"iso-8859-15\"";
    scores[3] = "text/plain; charset=\"iso-8859-2\"";
    scores[4] = "text/plain; charset=\"iso-8859-3\"";
    scores[5] = "text/plain; charset=\"iso-8859-4\"";
    scores[6] = "text/plain; charset=\"iso-8859-7\"";
    scores[7] = "text/plain; charset=\"us-ascii\"";
    scores[8] = "text/plain; charset=\"big5\"";
    scores[9] = "text/plain; charset=\"euc-kr\"";
    scores[10] = "text/plain; charset=\"ibm437\"";
    scores[11] = "text/plain; charset=\"ibm850\"";
    scores[12] = "text/plain; charset=\"Shift_JIS\"";
    scores[13] = "text/plain; charset=\"macintosh\"";
    scores[14] = "text/plain; charset=\"windows-1250\"";
    scores[15] = "text/plain; charset=\"windows-1251\"";
    scores[16] = "text/plain; charset=\"windows-1252\"";
    scores[17] = "text/plain; charset=\"windows-1253\"";
    scores[18] = "text/plain";
    scores[19] = "text/plain; charset=\"utf-16\"";
    scores[20] = "text/plain; charset=\"x-other\"";
}

{
    if ($1 == "etext_no") {
        # header line
        print $0;
    } else if (!($1 in seen) && ($3 ~ "text/plan*")) {
        if (pending != $1) {
            # new etext
            if (line != "")    { print line; }
            if (pending != "") { seen[pending] = true; }
            pending   = $1;
            line      = $0;
            cur_score = score($3);
        } else {
            # continuing previous etext
            if (score($3) < cur_score) {
                line      = $0;
                cur_score = score($3);
            }
        }
    }
}

END {
    if (line != "") { print line; }
}
