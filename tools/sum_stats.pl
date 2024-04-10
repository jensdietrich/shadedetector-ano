#!/usr/bin/perl

use strict;
use warnings;

my @order;
my %t;

foreach my $fName (@ARGV) {
	print STDERR "Reading $fName...\n";
	open(my $f, "<", $fName) or die $!;
	while (<$f>) {
		chomp;
		my ($k, $v) = split /=/;
		if (!exists $t{$k}) {
			push @order, $k;
			$t{$k} = 0;
		}

		$t{$k} += $v;
	}
}

foreach (@order) {
	print "$_=$t{$_}\n";
}
