#!/usr/bin/perl

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

# TODO: adjust timezones? if so, how to determine DST?

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
