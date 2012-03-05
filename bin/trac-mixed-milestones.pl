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

my $tickets = {};
my $due = {};

# query DB for ticket milestones
my $milestone_script = 'select id, milestone from ticket;';
my @milestones = `echo "$milestone_script" | sqlite3 "$db_path"`;
for my $token (@milestones) {
  chop $token;
  (my $id, my $milestone) = split(/\|/, $token);
  $tickets->{$id} = {};
  $tickets->{$id}->{'milestone'} = $milestone;
}

# query DB for ticket blockedby list
my $blockedby_script = "select ticket, value " .
  "from ticket_custom where name = 'blockedby';";
my @blockedby = `echo "$blockedby_script" | sqlite3 "$db_path"`;
for my $token (@blockedby) {
  chop $token;
  (my $id, my $blockedby) = split(/\|/, $token);
  $tickets->{$id}->{'blockedby'} = $blockedby;
}

# query DB for milestone due dates
my $due_date_script = 'select name, due from milestone;';
my @due_dates = `echo "$due_date_script" | sqlite3 "$db_path"`;
for my $token (@due_dates) {
  chop $token;
  (my $milestone, my $due_date) = split(/\|/, $token);
  $due->{$milestone} = $due_date;
}

# check tickets for mixed milestones
my $good_ticket_count = 0;
foreach my $id (sort {$a<=>$b} keys %$tickets) {
  my $bid = check($tickets, $due, $id);
  if ($bid == 0) {
    $good_ticket_count++;
  }
  else {
    print "$id: $bid\n";
  }
}
my $total_ticket_count = keys %$tickets;
if ($good_ticket_count < $total_ticket_count) {
  print "\nGood tickets: $good_ticket_count of $total_ticket_count\n";
  exit $good_ticket_count;
}
exit 0;

sub check($$$) {
  my $tickets = shift;
  my $due = shift;
  my $id = shift;

  my $ticket = $tickets->{$id};
  my $milestone = $ticket->{'milestone'};
  my $due_date = $due->{$milestone};
  my @blockedby = split(/, /, $ticket->{'blockedby'});
  for my $bid (@blockedby) {
    my $bid_milestone = $tickets->{$bid}->{'milestone'};
    my $bid_due_date = $due->{$bid_milestone};
    if ($due_date < $bid_due_date) {
      # parent milestone precedes child milestone
      return $bid;
    }
    my $result = check($tickets, $due, $bid);
    if ($result != 0) {
      # child has mixed milestones
      return $result;
    }
  }

  # all OK!
  return 0;
}
