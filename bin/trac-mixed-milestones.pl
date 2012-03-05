#!/usr/bin/perl

#
# trac-mixed-milestones.pl
#

# Script to detect linked tickets with differing milestones.
# Must be run on the server side.

use strict;

# parse command line arguments
my $db = '';
if ($#ARGV >= 0) {
  $db = $ARGV[0];
}
else {
  print "Please specify a Trac DB.\n";
  exit 1;
}
my $db_path = "/data/devel/trac/$db/db/trac.db";
unless (-e $db_path) {
  if ($db eq '') {
  }
  else {
    print "No such Trac DB: $db_path\n";
  }
  exit 2;
}

# query DB for ticket milestones
my $milestone_script = 'select id, milestone from ticket;';
my @milestones = `echo "$milestone_script" | sqlite3 "$db_path"`;

# query DB for ticket blockedby list
my $blockedby_script = "select ticket, value " .
  "from ticket_custom where name = 'blockedby';";
my @blockedby = `echo "$blockedby_script" | sqlite3 "$db_path"`;

# pack results from the DB into a nice hash
my $tickets = {};
for my $token (@milestones) {
  chop $token;
  (my $id, my $milestone) = split(/\|/, $token);
  $tickets->{$id} = {};
  $tickets->{$id}->{'milestone'} = $milestone;
}
for my $token (@blockedby) {
  chop $token;
  (my $id, my $blockedby) = split(/\|/, $token);
  $tickets->{$id}->{'blockedby'} = $blockedby;
}

# check tickets for mixed milestones
my $good_ticket_count = 0;
foreach my $id (sort {$a<=>$b} keys %$tickets) {
  if (check($tickets, $id)) {
    $good_ticket_count++;
  }
  else {
    print "$id\n";
  }
}
my $total_ticket_count = keys %$tickets;
print "\nGood tickets: $good_ticket_count of $total_ticket_count\n";

sub check($$) {
  my $tickets = shift;
  my $id = shift;

  my $ticket = $tickets->{$id};
  my $milestone = $ticket->{'milestone'};
  my @blockedby = split(/, /, $ticket->{'blockedby'});
  for my $bid (@blockedby) {
    if ($milestone ne $tickets->{$bid}->{'milestone'}) {
      # milestone does not match child
      return 0;
    }
    if (not check($tickets, $bid)) {
      # child has mixed milestones
      return 0;
    }
  }

  return 1;
}
