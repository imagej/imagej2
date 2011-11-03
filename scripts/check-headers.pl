#!/usr/bin/perl

# Script to verify existence and correctness of source code headers.

# Usage: perl scripts/check-headers.pl [subdirectory ...]

use strict;
my $dir = `dirname "$0"`;
chop $dir;
require "$dir/subs.pl";

my %knownAuthors = (
  "Adam Fraser" => 1,
  "Aivar Grislis" => 1,
  "Albert Cardona" => 1,
  "Barry DeZonia" => 1,
  "Curtis Rueden" => 1,
  "Grant Harris" => 1,
  "Johannes Schindelin" => 1,
  "Lee Kamentsky" => 1,
  "Rick Lentz" => 1,
  "Stephan Preibisch" => 1,
  "Stephan Saalfeld" => 1,
  "Wayne Rasband" => 1,
);

# parse command line arguments
my @args;
if (scalar @ARGV == 0) {
  @args = ('.');
}
else {
  @args = @ARGV;
}

# read copyright file
my @copyright = readFile("LICENSE.txt");
push(@copyright, '*/');
push(@copyright, '');

# find source files
my $cmd = "find @args -name '*.java'";
my @src = `$cmd`;

# process files
for my $file (@src) {
  chop $file;
  process($file);
}

sub process {
  my ($file) = @_;

  my $dir = `dirname "$file"`;
  chop $dir;
  my $base = `basename "$file"`;
  chop $base;
  my $class = substr($base, 0, length($base) - 5);

  # read in source file
  my @data = readFile($file);

  # check header comment
  my $i = 0;
  my @header = ('//', "// $base", '//', '', '/*');
  if (!match(\@header, \@data, $i)) {
    print "$file: invalid header\n";
    return;
  }
  $i += @header;

  # check copyright statement
  if (!match(\@copyright, \@data, $i)) {
    print "$file: invalid copyright\n";
    return;
  }
  $i += @copyright;

  # check package statement
  if ($data[$i++] !~ /^package .*;$/) {
    print "$file: invalid package\n";
    return;
  }

  # check import statements
  my $blank = 0;
  while (1) {
    my $line = trim($data[$i++]);
    if ($line =~ /^$/) {
      if ($blank) {
        print "$file: duplicate blank line at line #$i\n";
        return;
      }
      $blank = 1;
    }
    else {
      $blank = 0;
      if ($line eq '/**') {
        last;
      }
      elsif ($line !~ /\/\// && $line !~ /^import /) {
        print "$file: unexpected text at line #$i\n";
        return;
      }
    }
  }

  if ($data[$i] !~ /^ \* [^\s]/) {
    print "$file: malformed class comment at line #$i\n";
    return;
  }

  # check class comment
  my $author = 0;
  while (1) {
    my $line = $data[$i++];
    if ($line eq ' */') {
      last;
    }
    if ($line !~ /^ \* ?/) {
      print "$file: malformed class comment at line #$i\n";
      return;
    }
    if ($line =~ /^ \* \@author (.*)$/) {
      my $authorName = $1;
      if (!exists($knownAuthors{$authorName})) {
        print "$file: unknown author: $authorName\n";
        return;
      }
      $author = 1;
    }
  }

  if (!$author) {
    print "$file: missing author tag\n";
    return;
  }

  # skip annotations
  while ($data[$i] =~ /^\@/ || $data[$i] =~ /^\t/) {
    $i++;
  }

  # check type declaration
  if ($data[$i++] !~
    /^public (abstract )?(final )?(class)|(enum)|(interface) $class[ <]/)
  {
    print "$file: invalid type declaration at line #$i\n";
    return;
  }
}
