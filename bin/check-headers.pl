#!/usr/bin/perl

#
# check-headers.pl
#

# Script to verify existence and correctness of source code headers.

# Usage: bin/check-headers.pl [subdirectory ...]

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
  "Gabriel Landini" => 1,
  "Grant Harris" => 1,
  "Johannes Schindelin" => 1,
  "Lee Kamentsky" => 1,
  "Mark Hiner" => 1,
  "Rick Lentz" => 1,
  "Stephan Preibisch" => 1,
  "Stephan Saalfeld" => 1,
  "Wayne Rasband" => 1,

  "Jarek Sacha" => 1,
  "Melinda Green" => 1,
  "Sean Luke" => 1,
  "Sumit Dubey" => 1,
  "Werner Randelshofer" => 1,
  "Yap Chin Kiet" => 1,
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
my $spacers = 0;
for (my $i = 0; $i < @copyright; $i++) {
  if ($copyright[$i] eq '') {
    if ($spacers < 2) {
      $copyright[$i] = '%%';
      $spacers++;
    }
  }
  $copyright[$i] = ' * ' . $copyright[$i];
}
push(@copyright, ' * #L%');
push(@copyright, ' */');
push(@copyright, '');

# find source files
my $cmd = "find @args -name '*.java'";
my @src = `$cmd`;

# process files
my $rval = 0;
for my $file (@src) {
  chop $file;
  my $result = process($file);
  if ($result) { $rval = $result; }
}

exit $rval;

sub process($) {
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
  my @header = ('/*', ' * #%L');
  if (!match(\@header, \@data, $i)) {
    print "$file: invalid header\n";
    return 1;
  }
  $i += @header;

  # check copyright statement
  if (!match(\@copyright, \@data, $i)) {
    print "$file: invalid copyright\n";
    return 2;
  }
  $i += @copyright;

  # check for optional additional comments
  my $blank = 0;
  while (1) {
    if ($data[$i] =~ /^\/\//) {
      # single line comments are OK; continue
      $blank = 0;
    }
    elsif ($data[$i] =~ /^\/\*/) {
      # multi-line comments are OK; search for end of comment
      while ($i < @data && $data[$i++] !~ /\*\//) { }
      $blank = 0;
    }
    elsif ($data[$i] =~ /^\s*$/) {
      if ($blank) {
        print "$file: duplicate blank line at line #$i\n";
        return 3;
      }
      $blank = 1;
    }
    else {
      # not an additional comment; move on
      last;
    }
    $i++;
  }

  # check package statement
  if ($data[$i++] !~ /^package .*;$/) {
    print "$file: invalid package\n";
    return 4;
  }

  # check import statements
  $blank = 0;
  while (1) {
    my $line = trim($data[$i++]);
    if ($line =~ /^$/) {
      if ($blank) {
        print "$file: duplicate blank line at line #$i\n";
        return 5;
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
        return 6;
      }
    }
  }

  if ($data[$i] !~ /^ \* [^\s]/) {
    print "$file: malformed class comment at line #$i\n";
    return 7;
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
      return 8;
    }
    if ($line =~ /^ \* \@author (.*)$/) {
      my $authorName = $1;
      if (!exists($knownAuthors{$authorName})) {
        print "$file: unknown author: $authorName\n";
        return 9;
      }
      $author = 1;
    }
  }

  if (!$author) {
    print "$file: missing author tag\n";
    return 10;
  }

  # skip annotations
  while ($data[$i] =~ /^\@/ || $data[$i] =~ /^\t/) {
    $i++;
  }

  # check type declaration
  my $keywords = '(public )?(abstract )?(final )?(strictfp )?';
  if ($data[$i++] !~ /^$keywords(class)|(enum)|(interface) $class[ <]/) {
    print "$file: invalid type declaration at line #$i\n";
    return 11;
  }

  # all OK
  return 0;
}
