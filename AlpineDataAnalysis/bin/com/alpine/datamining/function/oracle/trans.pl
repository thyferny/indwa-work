$var = $ARGV[0];
open(FILE,$var);
$output = "";
$len = 0;
while(<FILE>)
{

	chomp;
	$line = trim($_);
	if ($line eq "/")
	{
		print $output."\n";
		print $line."\n";
		$output = "";
		$len = 0;
	}
	else
	{
		if ($line =~ "^--(.*)"){
		}else{
			if ($line =~ "(.*)--(.*)"){
				if ($len + 1 + length($1) > 2490){
					$output = $output."\n";
					$len = 0;
				}
				$len = $len + 1 + length($1);
				$output = $output." ".$1;
			}
			else
			{
				if ($len + 1 + length($line) > 2490){
					$output = $output."\n";
					$len = 0;
				}
				$len = $len + 1 + length($line);
				$output = $output." ".$line;
			}
		}
		
	}
}
print "\n";
close(FILE);
sub trim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

