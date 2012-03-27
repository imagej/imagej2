#!/usr/bin/perl

#
# fix-imglib-authors.pl
#

# Script to clean up author tags in ImgLib repository. Adapted from
# check-headers.pl, for one-time use transition to license-maven-plugin.

# Usage: bin/fix-imglib-authors.pl [subdirectory ...]

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

  # find authors
  my @newData;
  my @authors;
  my $authorIndex = -1;
  my $adjust = 0;
  for (my $i = 0; $i < @data; $i++) {
    # note location where authors should be inserted
    if ($authorIndex < 0 && $data[$i] =~ /^[a-z]+/ &&
      ($data[$i] =~ /interface / || $data[$i] =~ /class / ||
      $data[$i] =~ /enum /))
    {
      my $j = $i - 1;
      while ($j > 0 && ($data[$j] =~ /^\w*$/ || $data[$j] =~ /^\@/)) {
        # skip backwards to find the spot
        $j--;
      }
      if ($data[$j] !~ /^ \*\//) {
        # no javadoc comment terminator found; add a stub
        push(@newData, '/**');
        push(@newData, ' * TODO');
        push(@newData, ' */');
        $j = $i + 2;
      }
      $authorIndex = $j - $adjust;
    }

    # save author lines
    if ($i > 10 && ($data[$i] =~ /Copyright/ || $data[$i] =~ /\@author/)) {
      # reformat list of authors to individual @author annotations
      my $fixedLine = $data[$i];
      $fixedLine =~ s/.*Copyright *(\([Cc]\))? *[\d-]*,? *//;
      $fixedLine =~ s/.*\@author *//;
      $fixedLine =~ s/\.$//;
      my @list = split(/,|&| and /, $fixedLine);
      foreach my $l (@list) {
        $l = trim($l);
        push(@authors, " * \@author $l");
      }
      $adjust++;
      if ($data[$i + 1] =~ /^\s*\*\s*$/) {
        $i++;
        $adjust++;
      }
    }
    else {
      push(@newData, $data[$i]);
    }
  }

  if ($authorIndex < 0) {
    print "$file: No author index found!\n";
    return;
  }

  # remove duplicate authors
  my %authorSet;
  my @finalAuthors;
  foreach my $author (@authors) {
    if ($authorSet{$author} != 1) {
      push(@finalAuthors, $author);
      $authorSet{$author} = 1;
    }
  }

  # write out changed file
  open FILE, ">$file";
  for (my $i = $0; $i < @newData; $i++) {
    if ($i == $authorIndex) {
      if ($data[$i] !~ /^\s*\*\s*$/) {
        print FILE " *\n";
      }
      foreach my $author (@finalAuthors) {
        print FILE "$author\n";
      }
    }
    print FILE "$newData[$i]\n";
  }
  close(FILE);
}
