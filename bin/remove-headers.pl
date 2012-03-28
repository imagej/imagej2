#!/usr/bin/perl

#
# remove-headers.pl
#

# Script to strip old copyright headers. Adapted from check-headers.pl,
# for one-time use transition to license-maven-plugin.

# Usage: bin/remove-headers.pl [subdirectory ...]

use strict;

my $dir = `dirname "$0"`;
chop $dir;
require "$dir/subs.pl";

# parse command line arguments
my @args;
if (scalar @ARGV == 0) {
  @args = ('.');
}
else {
  @args = @ARGV;
}

# find source files
my $cmd = "find @args -name '*.java'";
my @src = `$cmd`;

# process files
for my $file (@src) {
  chop $file;
  process($file);
}

sub process($) {
  my ($file) = @_;

  my $dir = `dirname "$file"`;
  chop $dir;
  my $base = `basename "$file"`;
  chop $base;
  my $class = substr($base, 0, length($base) - 5);

  # read in source file
  my @data = readFile($file);

  # find where initial comment ends
  my $i = 0;
  while ($data[$i] =~ /^\/\// || $data[$i] =~ /^\s*$/) {
    # ignore leading single-line comments and blank lines
    $i++;
  }

  my @authors;
  if ($data[$i] =~ /^\/\*/) {
    # found copyright header
    while ($data[$i] !~ /\*\//) {
      # look for terminating string
      if ($data[$i] =~ /\@author/) { # || $data[$i] =~ /Copyright/) {
        # preserve author tag
        push(@authors, $data[$i]);
      }
      $i++;
    }
    $i++;
  }

  # write out changed file
  open FILE, ">$file";
  if ($data[$i] ne '' || @authors > 0) {
    print FILE "\n";
  }
  foreach (@authors) {
    print FILE "$_\n";
  }
  for (my $j = $i; $j < @data; $j++) {
    print FILE "$data[$j]\n";
  }
  close(FILE);
}
