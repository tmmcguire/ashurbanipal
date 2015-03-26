drop table if exists book_metadata;
create table book_metadata (
       etext_no            integer primary key,
       link                varchar(256) not null,
       title               text not null,
       author              text,
       subject             text,
       language            varchar(64),
       release_date        varchar(16),
       loc_class           text,
       notes               text,
       copyright_status    varchar(64)
);

drop table if exists nouns;
create table nouns (
       id          integer primary key serial,
       word        varchar(128) not null unique
);

drop table if exists topics;
create table topics (
       etext_no     integer not null references book_metadata (etext_no),
       word         integer not null references nouns (id),
       count        integer
);
drop index if exists topics_etext_no_idx;
create index topics_etext_no_idx on topics (etext_no);

drop table if exists parts_of_speech;
create table parts_of_speech (
       etext_no              integer primary key references book_metadata (etext_no),
       hash                  double precision,  -- #
       dollar                double precision,  -- $
       rquote                double precision,  -- ''
       comma                 double precision,  -- ,
       lrb                   double precision,  -- -LRB-
       rrb                   double precision,  -- -RRB-
       period                double precision,  -- .
       colon                 double precision,  -- :
       cc                    double precision,  
       cd                    double precision,  
       dt                    double precision,  
       ex                    double precision,  
       fw                    double precision,  
       prep                  double precision,  -- IN
       jj                    double precision,  
       jjr                   double precision,  
       jjs                   double precision,  
       ls                    double precision,  
       md                    double precision,  
       nn                    double precision,  
       nnp                   double precision,  
       nnps                  double precision,  
       nns                   double precision,  
       pdt                   double precision,  
       pos                   double precision,  
       prp                   double precision,  
       prp_dollar            double precision,  -- PRP$
       rb                    double precision,  
       rbr                   double precision,  
       rbs                   double precision,  
       rp                    double precision,  
       sym                   double precision,  
       to_                   double precision,  -- TO
       uh                    double precision,  
       vb                    double precision,  
       vbd                   double precision,  
       vbg                   double precision,  
       vbn                   double precision,  
       vbp                   double precision,  
       vbz                   double precision,  
       wdt                   double precision,  
       wp                    double precision,  
       wp_dollar             double precision,  -- WP$
       wrb                   double precision,  
       lquote                double precision   -- ``
);
