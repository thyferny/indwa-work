#! /bin/sh 

#  NAME
#       install.sh
# 
#  COPYRIGHT
#       Confidential Property of Alpine Data Labs.
#       Copyright 2011 by Alpine Data Labs.
#       All rights reserved.
# 
#  DESCRIPTION
#       Install script for Alpine Miner.
# 
#  The install script will
#  
#  1. Validate GreenPlum installation and ask the user
#     to confirm.
#     If GreenPlum installation can not be detected it
#     will prompt the user to input the path.
#  
#  2. Detect OS and architecture to decide which file
#     to copy.
#  
#  3. Run demo database setup commands.
#  
  
gppath=""
port="5432"

short_license_prompt() {
echo ""
echo "......"
echo ""
echo "********************************************************************************"
echo "	Do you accept the Alpine Miner license agreement? [yes | no]"
echo "********************************************************************************"
echo -n "(Or, type v to view the full license text.): "
}

# Show license agrrement.
#
#
show_license() {
	done=0
	mode="short"

	while [ "$done" = 0 ]
	do
		if [ "$mode" = "short" ]
		then 
			head -17 License.txt
			short_license_prompt
		else
			more License.txt
			mode="short"
		fi

		read ans
		case $ans in 
		yes)
			return
			;;
		no)
			exit 1
			;;
		v|view)
			mode="long"
			;;
		*)
			#unknown input, display again.
			;;
		esac

	done
}

  
# Determines if a program is in the user's PATH. 
#
# Arguments:
#    $1 the program to check
#
# Returns:
#    0 if found, else 1
#
internal_which() {
	binary="$1"

	for dir in `echo $PATH | tr ":" "\n"`; do
		if [ -s "$dir/$binary" -a -x "$dir/$binary" ]; then
			return 0
		fi
	done

	return 1
}

# Determines if path is relative.
#
# Returns:
#    0 if relative, otherwise 1.
#
is_relative() {
	path="$1"
	shift

	[ "${path:0:1}" != "/" ]
	return
}

# Determines whether the machine is 32 or 64-bit.
#
# Returns:
#    "32bit" or "64bit" on success with error code 0.  Exits with non-zero
#    status and undefined text on failure.
#
get_arch() {
	ELF_MAGIC=7f

	if [ "`od -N1 -An -t x1 < /bin/sh | tr -d ' '`" != "$ELF_MAGIC" ]; then
		exit 1
	fi

	arch=`od -j4 -N1 -An -t u1 < /bin/sh | tr -d ' '`

	case $arch in
	1)
		echo "32bit"
		exit 0
		;;
	2)
		echo "64bit"
		exit 0
		;;
	*)
		exit 1
		;;
	esac
}


# Determines the OS o the machine
#
# Returns:
#	centos_32bit
#	centos_64bit
#	osx_32bit
#	solaris_64bit
#	suse_64bit
# or error exit.
#
get_os() {

	OS=`uname -s`
        arch=''

	if [ "$OS" = "Linux" ]
	then
		arch=`get_arch`

		# New determine Linux version.
		ver=`lsb_release -i | awk '{print $3}' | tr '[A-Z]' '[a-z]'`
		if [ "$ver" = "centos" ]
		then
			echo "$ver"_"$arch"
		elif [ "$ver" = "suse" ]
		then
			echo "suse_64bit"
		elif [ "${ver:0:6}" = "redhat" ]
		then
			echo "centos"_"$arch"
		else
			# unknown version
			exit 1
		fi
	elif [ "$OS" = "SunOS" ]
	then
		echo "solaris_64bit"
	elif [ "$OS" = "Darwin" ]
	then
		echo "osx_32bit"
	else
		# unknown OS
		exit 1
	fi
}

# Get default GreenPlum install path
#
# void.
#
get_default_greenplum_install() {
	gppath="$GPHOME"

	if [ -d "$gppath" -a -x "$gppath/bin/psql" ]
	then
		return
	fi 

	# if not there check for the usual places.
	gppath=/usr/local/greenplum-db
	if [ -d "$gppath" -a -x "$gppath/bin/psql" ]
	then
		return
	fi 

	gppath=/opt/greenplum-db
	if [ -d "$gppath" -a -x "$gppath/bin/psql" ]
	then
		return
	fi 

	# Still not found
	gppath=""
}

# Validate GreenPlum install path
#
# void.
#
validate_greenplum_install() {

	get_default_greenplum_install

	if [ "$gppath" != "" ]
	then
		echo "*****************************************************************"
		echo "	Alpine Miner Installer has detected GreenPlum istallation"
		echo "	path: $gppath"
		echo ""
		echo "	Provide the installation path for Greenplum Database"
		echo "	or press ENTER to accept the default"
		echo "*****************************************************************"
		echo -n "Greenplum installation path: "
		read ans
		if [ "$ans" = "" ]
		then
			return
		else
			gppath=$ans
		fi
	fi

	# Now prompt the user to input path
	if [ "$gppath" = "" ]
	then
		echo -n "Please input GreenPlum installation path: "
		read gppath
	fi

	while [ 1 ]
	do
		if [ "$gppath" = "" ]
		then
			exit 1
		fi
		if [ -d "$gppath" -a -x "$gppath/bin/psql" ]
		then
			return
		fi 
		echo "Path $gppath is not a valide GreenPlum installation, try again"
		read gppath
	done
}

# Copy shared lib into GreenPlum installation
#
# void
#
copy_sharedlib_file() {

	# determine which version of GreenPlum 4.0 or 4.1 or 4.2
	#
	# WARNING: This code need to be updated when
   	# 	   supporting newer version of GreenPlum
	#
	srcPrefix="sharedLib"
	ver=`grep 'GP_VERSION ' "$gppath/include/pg_config.h" |grep '4\.1\.'`
	if [ "$ver" != "" ]
	then
		srcPrefix="sharedLib/4.1"
	fi

	ver=`grep 'GP_VERSION ' "$gppath/include/pg_config.h" |grep '4\.2\.'`
	if [ "$ver" != "" ]
	then
		srcPrefix="sharedLib/4.2"
	fi


	os=`get_os`
	srcFile="$srcPrefix/alpine_miner."${os}".so"
	destFile="$gppath/lib/postgresql/alpine_miner.so"

	echo "Copying $srcFile to $destFile"
	doCopy="y"
	if [ ! -f $srcFile ]
	then
		echo "Shared library file $srcFile does not exist."
		echo "exiting..."
		exit 1
	fi

	if [ -f $destFile ]
	then

	echo "*****************************************************************"
	echo "File $destFile exists, override? (y/n) "
	echo "*****************************************************************"
	echo -n "Answer [y]: "

		read ans
		if [ "$ans" = "n" ]
		then
			doCopy="n"
		fi
	fi

	if [ "$doCopy" = "y" ]
	then
		rm -f $destFile
		cp  $srcFile $destFile
		chmod 755 $destFile
	fi

	echo "*****************************************************************"
	echo "Copy shared library to segment hosts? (y/n) "
	echo "*****************************************************************"
	echo -n "Answer [y]: "

	read ans
	if [ "$ans" != "n" ]
	then
		echo -n "Please enter the FULL PATH of the hostfile name: "
		read hosts
		$gppath/bin/gpscp -f $hosts $destFile =:"$destFile"
	fi

}
# Read greenplum port
#
#void
#
setup_greenplum_port(){
        echo "*****************************************************************"
        echo "  Provide the Greenplum Database port"
        echo "  or press ENTER to accept the default: $port"
        echo "*****************************************************************"
        echo -n "port [$port]: "
        read ans
        if [ "$ans" != "" ]
        then
                port=$ans
        fi
}

# Install Alpine Miner and create demo database.
#
# void
#
setup_demo_db () {
	CMD="psql"
	
	#Check if the command is available
	internal_which $CMD
	if [ $? -eq 1 ]
	then
		CMD="$gppath/bin/psql"
	fi

	default_db="template1"
	echo "*****************************************************************"
	echo "	Provide the Greenplum Database name"
	echo "	or press ENTER to accept the default: $default_db"
	echo "*****************************************************************"
	echo -n "Database name [$default_db]: "
	read ans
	if [ "$ans" != "" ]
	then
		default_db=$ans
	fi
	setup_greenplum_port;
	echo $CMD -p $port -d $default_db -c '"CREATE PROCEDURAL LANGUAGE plpgsql;"'
	$CMD  -p $port -d $default_db -c 'CREATE PROCEDURAL LANGUAGE plpgsql;'
	echo $CMD -p $port -d $default_db -f alpine_miner_setup.sql
	$CMD -p $port -d $default_db -f alpine_miner_setup.sql

	echo "*****************************************************************"
	echo "	Would you like to install Alpine Miner demo database? (y/n) "
	echo "*****************************************************************"
	echo -n "Answer [y]: "
	read ans
	if [ "$ans" != "n" ]
	then
		$CMD -p $port -d $default_db -f create_demo_db.sql
		$gppath/bin/gpstop -u 
	fi
	
}

main () {
	show_license
	validate_greenplum_install
#	is_relative $gppath
#	if [ $? -eq 0 ]
#	then
#		gppath="./$gppath"
#	fi
	copy_sharedlib_file
	setup_demo_db
	echo "Done."
}

main "$@"



