#!/usr/bin/perl

# Shared helper subroutines for use by perl scripts.

sub match($$$) {
  my ($tRef, $dRef, $index) = @_;
  my @template = @$tRef;
  my @data = @$dRef;

  my $i = $index;
  my $result = 1;
  for my $expected (@template) {
    my $actual = $data[$i++];
    if ($actual ne $expected) {
      $result = 0;
      last;
    }
  }
  return $result;
}

sub readFile($) {
  my ($file) = @_;
  open FILE, "$file" or die "$file: $!";
  my @data = <FILE>;
  close(FILE);
  for my $line (@data) {
    chop $line;
  }
  return @data;
}

sub trim($) {
  my ($line) = @_;
  $line =~ s/^\s*//;
  $line =~ s/\s*$//;
  return $line;
}

1
