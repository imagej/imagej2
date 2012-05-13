#!/usr/bin/perl

# read authors.txt
our %authors = {};
open ($in, '<authors.txt');
while (<$in>) {
	if (/^(.*?)\s*=\s*(.*)$/) {
		$authors{$1} = $2;
	}
}
close($in);

# TODO: adjust timezones? if so, how to determine DST?

sub rewrite_author($$) {
	my $author = $_[0];
	my $headers = $_[1];

	if ($author eq '' && $headers =~ /^author (\S+)/m) {
		$author = $authors{$1};
	}

	if ($author eq '') {
		die('Could not determine author from headers ' . $headers);
	}

	$headers =~ s/^(author )\S+ <\S+>( .*)$/\1$author\2/m;
	$headers =~ s/^(committer )\S+ <\S+>( .*)$/\1$author\2/m;
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
				$msg = $_;
				while (<$in>) {
					$msg .= $_;
					$count -= length($_);
					last if ($count <= 0);
				}
				last;
			}
			$headers .= $_;
		}

		$headers = rewrite_author('', $headers);
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
