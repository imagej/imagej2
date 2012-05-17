#!/usr/bin/perl

use DateTime;

# read authors.txt
our %authors = {};
open ($in, '<authors.txt');
while (<$in>) {
	if (/^(.*?)\s*=\s*(.*)$/) {
		my $nick = $1;
		my $ident = $2;
		if ($ident =~ /^(.* )(<.*>)$/) {
			$ident = $1 . lc($2);
		}
		$authors{$nick} = $ident;
	}
}
close($in);

our %timezones = (
	'Aivar Grislis' => 'America/Chicago',
	'Barry DeZonia' => 'America/Chicago',
	'Curtis Rueden' => 'America/Chicago',
	'Johannes Schindelin' => 'America/Chicago',
	'Mark Hiner' => 'America/Chicago',
	'Melissa Linkert' => 'America/Chicago',
	'Rick Lentz' => 'America/Chicago',
	'Adam Fraser' => 'America/Detroit',
	'Grant Harris' => 'America/Detroit',
	'Lee Kamentsky' => 'America/Detroit'
);

sub get_timezone($$) {
	my $epoch = $_[0];
	my $person = $_[1];
	my $timezone = $timezones{$person};

	die('Could not determine time zone for ' . $person) if ($timezone eq '');

	my $datetime = DateTime->from_epoch('epoch' => $epoch);
	$datetime->set_time_zone($timezone);
	return $datetime->strftime('%z');
}

sub rewrite_author($$) {
	my $author = $_[0];
	my $headers = $_[1];

	my $committer;
	if ($headers =~ /^committer (\S+)/m) {
		$committer = $authors{$1};
		die('Could not determine committer from headers ' . $headers)
			if ($committer eq '');
	}
	$author = $committer if ($author eq '');

	$headers =~ s/^(author )\S+ <\S+>( .*)$/\1$author\2/m;
	$headers =~ s/^(committer )\S+ <\S+>( .*)$/\1$committer\2/m;

	# adjust timezone
	$headers =~ s/^((author|committer)\s+([^<]+?)\s+<.*>\s+(\d+)\s+)[-+]\d{4}$/$1 . get_timezone($4, $3)/meg;

	return $headers;
}

my @export = ('git', 'fast-export', '--no-data', 'refs/remotes/trunk');
my @import = ('git', 'fast-import');
open (my $in, '-|', @export);
open (my $out, '|-', @import);
print $out "reset refs/heads/master.new\n";
while (<$in>) {
	if (/^(commit .*)/) {
		print $out "commit refs/heads/master.new\n";

		# read headers and commit message
		my $headers = '';
		my $msg = '';
		while (<$in>) {
			if (/^data (.*)/) {
				my $count = $1;
				$msg = '';
				while (<$in>) {
					$msg .= $_;
					$count -= length($_);
					last if ($count <= 0);
				}
				last;
			}
			$headers .= $_;
		}

		my $author = '';
		if ($msg =~ /^(.*\n)Authored-by: ([^\n]*)\s*(.*)/s) {
			$msg = $1 . $3;
			$author = $2;
			$author = $authors{'leek'}
				if ($author eq 'Lee Kamentsky');
		}
		$headers = rewrite_author($author, $headers);

		$msg =~ s/^git-svn-id: .*trunk@(\d+) .*$/This used to be revision r\1./m;

		if ($msg =~ /^Release version 2.0.0-beta2/m) {
			$msg = "Failed attempt at 2.0.0-beta2 release\n\n"
				. "Due to various issues, this commit does *not* represent the state of the\n"
				. "codebase for the actual 2.0.0-beta2 release. Please use the v2.0.0-beta2\n"
				. "tag instead.\n\n"
				. "This used to be revision r5392.\n";
		}
		$msg = 'data ' . length($msg) . "\n" . $msg;
		print $out $headers;
		print $out $msg;

		# read the rest of the commit
		while (<$in>) {
			print $out $_;
			last if (/^$/);
		}
	}
}
close($in);
close($out);
