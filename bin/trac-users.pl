#!/usr/bin/perl

#
# trac-users.pl
#

# A script for managing Trac user information (names and email addresses)
# across multiple Trac environments.
# Must be run with appropriate permissions on the server side.

# Thanks to: http://trac.edgewall.org/ticket/2245

use strict;

# declare global variables
my $tracPrefix = '/var/lib/trac';
my @tracs;
my %userNames, my %userEmails;

# parse command line arguments
my $cmd = '--show';
if (@ARGV > 0) {
  $cmd = $ARGV[0];
}

# execute appropriate command
if ($cmd eq '--show') {
  cmdShow();
}
elsif ($cmd eq '--fix') {
  cmdFix();
}
elsif ($cmd eq '--purge') {
  cmdPurge();
}
elsif ($cmd eq '--set') {
  cmdSet();
}
elsif ($cmd eq '--delete') {
  cmdDelete();
}
else {
  if ($cmd ne '--help') {
    print "Unknown command: $cmd\n";
    print "\n";
  }
  cmdUsage();
}

# -- Command functions --

sub cmdShow() {
  harvestUserInfo();

  foreach my $id (keys %userEmails) {
    if (not defined $userNames{$id}) {
      $userNames{$id} = '(Unknown)';
    }
  }

  foreach my $id (sort keys %userNames) {
    print "$id = $userNames{$id}";
    if (defined $userEmails{$id}) {
      print " <$userEmails{$id}>";
    }
    print "\n";
  }
}

sub cmdFix() {
  harvestUserInfo();

  foreach my $id (sort keys %userNames) {
    my $name = $userNames{$id};
    my $email = $userEmails{$id};
    if (defined $email) {
      print "$id => $name <$email>:\n";
      foreach my $trac (@tracs) {
        dbAssign($trac, $id, $name, $email);
      }
    }
    else {
      print "$id: skipped\n";
    }
  }
}

sub cmdPurge() {
  harvestUserInfo();

  foreach my $id (sort keys %userNames) {
    if ($id !~ /^[0-9a-f]{24}$/) {
      # skip legitimate users
      next;
    }
    foreach my $trac (@tracs) {
      dbDelete($trac, $id);
    }
    print "DELETE $id\n";
  }
}

sub cmdSet() {
  my $id, my $newName, my $newEmail;
  if (@ARGV == 4) {
    $id = $ARGV[1];
    $newName = $ARGV[2];
    $newEmail = $ARGV[3];
  }
  else {
    print STDERR "Error: invalid arguments: @ARGV\n\n";
    cmdUsage();
    return;
  }

  harvestUserInfo();

  print "$id => $newName <$newEmail>:\n";
  foreach my $trac (@tracs) {
    dbAssign($trac, $id, $newName, $newEmail);
  }
}

sub cmdDelete() {
  my $id;
  if (@ARGV == 2) {
    $id = $ARGV[1];
  }
  else {
    print STDERR "Error: invalid arguments: @ARGV\n\n";
    cmdUsage();
    return;
  }

  @tracs = <$tracPrefix/*>;
  foreach my $trac (@tracs) {
    dbDelete($trac, $id);
  }
  print "DELETE $id\n";
}

sub cmdUsage() {
  print "Usage:\n";
  print "  trac-users.pl [--show]\n";
  print "    Displays info on known users\n";
  print "\n";
  print "  trac-users.pl --fix\n";
  print "    Standardizes user info across all Trac environments\n";
  print "\n";
  print "  trac-users.pl --purge\n";
  print "    Deletes invalid/spam users\n";
  print "\n";
  print "  trac-users.pl --set username 'New Name' 'new\@email.address'\n";
  print "    Sets info for the given username\n";
  print "\n";
  print "  trac-users.pl --delete username\n";
  print "    Deletes info for the given username\n";
  exit 1;
}

# -- Helper functions --

sub harvestUserInfo() {
  @tracs = <$tracPrefix/*>;
  foreach my $trac (@tracs) {
    my @names = dbRetrieveAll($trac, 'name');
    my @emails = dbRetrieveAll($trac, 'email');
    foreach my $name (@names) {
      chop $name;
      (my $id, my $value) = split(/\|/, $name);
      if (defined $userNames{$id} && $userNames{$id} ne $value) {
        print STDERR "Warning: $trac: inconsistent name for $id: " .
          "'$userNames{$id}' != '$value'\n";
      }
      $userNames{$id} = $value;
    }
    foreach my $email (@emails) {
      chop $email;
      (my $id, my $value) = split(/\|/, $email);
      if (defined $userEmails{$id} && $userEmails{$id} ne $value) {
        print STDERR "Warning: $trac: inconsistent email for $id: " .
          "'$userEmails{$id}' != '$value'\n";
      }
      $userEmails{$id} = $value;
    }
  }

  foreach my $id (sort keys %userNames) {
    if (not defined $userEmails{$id}) {
      print STDERR "Warning: no email for $id ($userNames{$id})\n";
    }
  }

  foreach my $id (sort keys %userEmails) {
    if (not defined $userNames{$id}) {
      print STDERR "Warning: no name for $id ($userEmails{$id})\n";
    }
  }
}

sub dbRetrieve($$$) {
  my $trac = shift;
  my $id = shift;
  my $attr = shift;
  my $cmd = "select value from session_attribute " .
    "where sid='$id' and name='$attr';";
  my $result = dbCommand($trac, $cmd);
  chop $result;
  return $result;
}

sub dbRetrieveAll($$) {
  my $trac = shift;
  my $attr = shift;
  my $cmd = "select sid, value from session_attribute where name='$attr';";
  return dbCommand($trac, $cmd);
}

sub dbAssign($$$$) {
  my $trac = shift;
  my $id = shift;
  my $name = shift;
  my $email = shift;

  my $nameData = dbRetrieve($trac, $id, 'name');
  my $emailData = dbRetrieve($trac, $id, 'email');

  if ($nameData eq '') {
    print "\t$trac: INSERT name\n";
    dbInsert($trac, $id, 'name', $name);
  }
  elsif ($nameData ne $name) {
    print "\t$trac: UPDATE name\n";
    dbUpdate($trac, $id, 'name', $name);
  }
  if ($emailData eq '') {
    print "\t$trac: INSERT email\n";
    dbInsert($trac, $id, 'email', $email);
  }
  elsif ($emailData ne $email) {
    print "\t$trac: UPDATE email\n";
    dbUpdate($trac, $id, 'email', $email);
  }
}

sub dbDelete($$) {
  my $trac = shift;
  my $id = shift;
  my $cmd = "delete from session_attribute where sid='$id';";
  dbCommand($trac, $cmd);
}

sub dbUpdate($$$$) {
  my $trac = shift;
  my $id = shift;
  my $attr = shift;
  my $value = shift;
  my $cmd = "update session_attribute set value='$value' " .
    "where sid='$id' and name='$attr';";
  dbCommand($trac, $cmd);
}

sub dbInsert($$$$) {
  my $trac = shift;
  my $id = shift;
  my $attr = shift;
  my $value = shift;
  my $cmd = "insert into session_attribute " .
    "(sid, authenticated, name, value) values ('$id', 1, '$attr', '$value');";
  dbCommand($trac, $cmd);
}

sub dbCommand($$) {
  my $trac = shift;
  my $cmd = shift;
  return `echo "$cmd" | sqlite3 "$trac/db/trac.db"`;
}
