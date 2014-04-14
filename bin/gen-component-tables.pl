#!/usr/bin/perl

#
# gen-component-tables.pl
#

# Script to generate an HTML table describing ImageJ software components.
# Information is recursively scraped from Maven POM files.
# Requires XML::Simple to be installed.

# Usage: bin/gen-component-tables.pl > out.html

use strict;
use XML::Simple;

my $dir = `dirname "$0"`;
chop $dir;
require "$dir/subs.pl";

chdir "$dir/..";

# -- Constants --

my $nameColor = '#d8ecf8';
my $descColor = '#eeeeee';
my $columns = 4;

my $jenkinsURL = 'http://jenkins.imagej.net';
my $githubURL = 'https://github.com/imagej/imagej';

my $artifactPath = 'lastSuccessfulBuild/artifact';
my $sourcePath = 'src/main/java';

my $trunkBuildPrefix = "$jenkinsURL/job/ImageJ/$artifactPath";
my $dailyBuildPrefix = "$jenkinsURL/job/ImageJ-daily/$artifactPath";
my $sourcePrefix = "$githubURL/tree/master";

# -- Main --

{
  section('App', 'app');
  section('Core', 'core');
  section('UI: AWT &amp; Swing', 'plugins/uis/awt-swing');
  section('UI: AWT', 'plugins/uis/awt');
  section('UI: Swing', 'plugins/uis/swing');
}

# -- Subroutines --

sub section($$) {
  my $title = shift;
  my $dirs = shift;

  # print section header
  print "<h3>$title</h3>\n\n";

  print "<table border=\"0\">\n";
  print "<tbody><tr>\n";
  print "<th>Trunk build</th>\n";
  print "<th>Daily build</th>\n";
  print "<th>Maven site</th>\n";
  print "<th>Browse source</th>\n";
  print "</tr>\n";

  my @poms = `find $dirs -name 'pom.xml' | grep -v '/target/'`;
  foreach my $pom (@poms) {
    chop $pom;
    process($pom);
  }

  # print section footer
  print "</tbody></table>\n\n";
}

# Processes the given POM file.
sub process($) {
  my ($file) = @_;

  # read in POM file
  my $ref = XMLin($file);

  # skip multi-module POMs
  if ($ref->{modules}) {
    return;
  }

  # parse information from the XML structure
  my $artifactId = $ref->{artifactId};
  my $groupId = $ref->{parent}->{groupId};
  my $version = $ref->{parent}->{version};
  my $name = $ref->{name};
  my $description = $ref->{description};

  # synthesize information from known values
  my $componentPath = $file;
  $componentPath =~ s/\/pom.xml$//;
  my $groupPath = $groupId;
  $groupPath =~ s/\./\//g;

  # scan for Java files to determine package prefix
  my $packagePathPrefix = "$componentPath/$sourcePath";
  my $packagePath = $packagePathPrefix;
  while (1) {
    my @java = <$packagePath/*.java>;
    if (@java > 0) {
      # found source file(s)
      last;
    }
    my @files = <$packagePath/*>;
    if (@files != 1) {
      last;
    }
    $packagePath = $files[0];
  }
  my $package = $packagePath;
  $package =~ s/$packagePathPrefix\/?//;
  $package =~ s/\//./g;

  my $jarFile = "$artifactId-$version.jar";
  my $jarName = "$artifactId.jar";
  my $trunkBuild = "$trunkBuildPrefix/$componentPath/target/$jarFile";
  my $dailyBuild = "$dailyBuildPrefix/$componentPath/target/$jarFile";
  my $mavenSite = "$dailyBuildPrefix/$componentPath/target/site/index.html";
  my $sourceCode = "$sourcePrefix/$packagePath";

  print "<tr><td style=\"background-color: $nameColor;\" " .
    "colspan=\"$columns\">$name</td></tr>\n";
  print "<tr>\n";
  print "<td><a href=\"$trunkBuild\">$jarName</a></td>\n";
  print "<td><a href=\"$dailyBuild\">$jarName</a></td>\n";
  print "<td><a href=\"$mavenSite\">$artifactId</a></td>\n";
  print "<td><a href=\"$sourceCode\">$package</a></td>\n";
  print "</tr>\n";
  print "<tr><td style=\"background-color: $descColor; font-size: smaller\"" .
    "colspan=\"$columns\">$description</td></tr>\n";
  print "<tr><td colspan=\"$columns\">&nbsp;</td></tr>\n";

}
