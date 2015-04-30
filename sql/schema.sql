-- -*- sql-product: postgres; -*-


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


-- Compute the euclidian distance between the parts_of_speech row identified by orig_no and another POS row.
-- Use:
--   select pos.etext_no, pos_distance(773, pos.*) as dist from parts_of_speech pos order by dist;
create or replace function pos_distance(orig_no integer, dest parts_of_speech) returns double precision as $$
declare
  orig parts_of_speech%rowtype;
  acc double precision;
begin
  select * into orig from parts_of_speech where etext_no = orig_no;
  acc :=       (orig.hash - dest.hash)^2;
  acc := acc + (orig.dollar - dest.dollar)^2;
  acc := acc + (orig.rquote - dest.rquote)^2;
  acc := acc + (orig.comma - dest.comma)^2;
  acc := acc + (orig.lrb - dest.lrb)^2;
  acc := acc + (orig.rrb - dest.rrb)^2;
  acc := acc + (orig.period - dest.period)^2;
  acc := acc + (orig.colon - dest.colon)^2;
  acc := acc + (orig.cc - dest.cc)^2;
  acc := acc + (orig.cd - dest.cd)^2;
  acc := acc + (orig.dt - dest.dt)^2;
  acc := acc + (orig.ex - dest.ex)^2;
  acc := acc + (orig.fw - dest.fw)^2;
  acc := acc + (orig.prep - dest.prep)^2;
  acc := acc + (orig.jj - dest.jj)^2;
  acc := acc + (orig.jjr - dest.jjr)^2;
  acc := acc + (orig.jjs - dest.jjs)^2;
  acc := acc + (orig.ls - dest.ls)^2;
  acc := acc + (orig.md - dest.md)^2;
  acc := acc + (orig.nn - dest.nn)^2;
  acc := acc + (orig.nnp - dest.nnp)^2;
  acc := acc + (orig.nnps - dest.nnps)^2;
  acc := acc + (orig.nns - dest.nns)^2;
  acc := acc + (orig.pdt - dest.pdt)^2;
  acc := acc + (orig.pos - dest.pos)^2;
  acc := acc + (orig.prp - dest.prp)^2;
  acc := acc + (orig.prp_dollar - dest.prp_dollar)^2;
  acc := acc + (orig.rb - dest.rb)^2;
  acc := acc + (orig.rbr - dest.rbr)^2;
  acc := acc + (orig.rbs - dest.rbs)^2;
  acc := acc + (orig.rp - dest.rp)^2;
  acc := acc + (orig.sym - dest.sym)^2;
  acc := acc + (orig.to_ - dest.to_)^2;
  acc := acc + (orig.uh - dest.uh)^2;
  acc := acc + (orig.vb - dest.vb)^2;
  acc := acc + (orig.vbd - dest.vbd)^2;
  acc := acc + (orig.vbg - dest.vbg)^2;
  acc := acc + (orig.vbn - dest.vbn)^2;
  acc := acc + (orig.vbp - dest.vbp)^2;
  acc := acc + (orig.vbz - dest.vbz)^2;
  acc := acc + (orig.wdt - dest.wdt)^2;
  acc := acc + (orig.wp - dest.wp)^2;
  acc := acc + (orig.wp_dollar - dest.wp_dollar)^2;
  acc := acc + (orig.wrb - dest.wrb)^2;
  acc := acc + (orig.lquote - dest.lquote)^2;
  return acc;  
end;
$$ language plpgsql;

-- Sort books by distance (in part-of-speech space) from a given etext, orig. See pos_distance above.
--
-- Use:
-- select * from pos_distances(2701) pd inner join book_metadata bm on (pd.etext_no = bm.etext_no) order by pd.dist;
create or replace function pos_distances(orig integer) returns table(etext_no integer, dist double precision) as $$
begin
  return query select pos.etext_no, pos_distance(orig, pos.*) as dist from parts_of_speech pos order by dist;
end;
$$ language plpgsql;

-- By score: The score for two texts, A and B, is one minus the
-- average of the scores for each topic word in the insersection of A
-- and B, summed across the words. The score for a text and itself
-- will be 0.0; the score for two completely different texts will be
-- 1.0.
-- 
-- Use:
-- select * from topic_scores(773) ts inner join book_metadata bm on (ts.etext_no = bm.etext_no) order by score;

create or replace function topic_scores(orig integer) returns table(etext_no integer, score double precision) as $$
begin
  return query select scores.etext_no, 1 - sum(score1 + score2) / 2 as score
  from (
    select t1.etext_no,t1.score as score1,t2.score as score2
    from topics t1 inner join (
      select topics.word, topics.score from topics where topics.etext_no = orig
    ) t2 on (t1.word = t2.word)
  ) scores group by scores.etext_no order by score desc;
end;
$$ language plpgsql;

-- By count: The count is simply one minus the number of topic words
-- in the intersection of two texts, A and B, divided by the number of
-- possible words, 200. The score for a text and itself will be 0.0;
-- the score for two completely different texts will be 1.0.
-- 
-- Use:
-- select * from topic_counts(773) tc inner join book_metadata bm on (tc.etext_no = bm.etext_no) order by score;

create or replace function topic_counts(orig integer) returns table(etext_no integer, score numeric) as $$
begin
  return query select topics.etext_no, 1 - count(topics.word) / 200.0 as score
  from topics inner join (
       select topics.word from topics where topics.etext_no = orig
  ) sq on (topics.word = sq.word) group by topics.etext_no order by score desc;
end;
$$ language plpgsql;

-- Sort books by a combination of style and topic. The combination is
-- currently (score*score) * pos_distance.
--
-- Use:
-- select * from combination_scores(773) tc inner join book_metadata bm on (tc.etext_no = bm.etext_no) order by dist_score;
create or replace function combination_scores(orig integer) returns table(etext_no integer, dist_score double precision) as $$
begin
  return query select
    topics.etext_no,
    topics.score * topics.score * styles.dist as dist_score
  from
    topic_scores(orig) topics
    inner join pos_distances(orig) styles on (styles.etext_no = topics.etext_no)
  order by dist_score;
end;
$$ language plpgsql;
