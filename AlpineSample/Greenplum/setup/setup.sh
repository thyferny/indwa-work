#!/bin/bash

bit=`uname -m`

#echo $bit
#	i686 means 32-bit
#	x86_64 means 64-bit

os=`uname`
#echo $os
#Linux
#Darwin
#SunOS



if [ "$os" = "Darwin" ]; then
soFile="alpine_miner.osx_32bit.so"
else
	if [ "$os" = "SunOS" ]
	then
	soFile="alpine_miner.solaris_64bit.so"
	else
                isCentOS=`cat /etc/issue|grep -i "CentOS"`
		length=`expr length "$isCentOS"`
		 
		if [  "$length" -gt "0" ] 
		then
			if [ "$bit" = "i686" ]
			then
				soFile="alpine_miner.centos_32bit.so"
			else
				soFile="alpine_miner.centos_64bit.so"
			fi
		else
			soFile="alpine_miner.suse_64bit.so"
		fi
	fi
fi

#echo "Will using $soFile"

#is null 
#distribution=`cat /etc/issue`
#echo $distribution
#CentOS release 5.4 (Final)
#Welcome to SUSE Linux Enterprise Server 10 SP2 (x86_64) - Kernel \r (\l).

#cp ./sharedLib/$soFile $GPHOME/lib/postgresql


PS3="Please select the GPHOME directory(input 1 or 2):"

OPTIONS="$GPHOME Others" 
 
select selected in $OPTIONS; do
	case $selected in 
                $GPHOME) targetDir=$GPHOME   ;break;; 
                "Others") 	echo "Please input the GPHOME directory:";
                			read targetDir;break;; 
                *) echo "error,please input again";
    esac 
done 

if [ ! -d "$targetDir" ]
then
	echo "$targetDir not exists"; exit 1;
	else
		targetDir=$targetDir/lib/postgresql
fi
 
subdir="./sharedLib"
echo "cp $subdir/$soFile  $targetDir/alpine_miner.so"
cp "$subdir/$soFile"  "$targetDir/alpine_miner.so" 

# now copy so file to segment... using segment.host
#if have segments...
#filesize=`stat -c%s ./segment.host`
#if [ "$filesize" -gt "0" ]
if [ -s ./segment.host ]
then
	echo " copy so file to segments defined in segment.host "
	gpscp -f ./segment.host "$targetDir/alpine_miner.so"  =:$GPHOME/lib/postgresql
fi

psql -d template1 -c "CREATE PROCEDURAL LANGUAGE plpgsql ;"
psql -d template1 -f alpine_miner_setup.sql

echo "Server set up finished."