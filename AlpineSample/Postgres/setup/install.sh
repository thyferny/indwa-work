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
#  1. Validate Postgres installation and ask the user
#     to confirm.
#     If Postgres installation can not be detected it
#     will prompt the user to input the path.
#  
#  2. Detect OS and architecture to decide which file
#     to copy.
#  
#  3. Run demo database setup commands.
#  
  
pgpath=""
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

# Show license agreement.
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
	elif [ "$OS" = "Darwin" ]
	then
		echo "osx_32bit"
	else
		# unknown OS
		exit 1
	fi
}

# Get default Postgres install path
#
# void.
#
get_default_postgres_install() {
	pgpath="$PGHOME"

	if [ -d "$pgpath" -a -x "$pgpath/bin/psql" ]
	then
		return
	fi 

	# if not there check for the usual places.
	pgpath=/usr/local/postgres
	if [ -d "$pgpath" -a -x "$pgpath/bin/psql" ]
	then
		return
	fi 

	pgpath=/opt/postgres
	if [ -d "$pgpath" -a -x "$pgpath/bin/psql" ]
	then
		return
	fi 

	# Still not found
	pgpath=""
}

# Validate Postgres install path
#
# void.
#
validate_postgres_install() {

	get_default_postgres_install

	if [ "$pgpath" != "" ]
	then
		echo "*****************************************************************"
		echo "	Alpine Miner Installer has detected Postgres istallation"
		echo "	path: $pgpath"
		echo ""
		echo "	Provide the installation path for Postgres Database"
		echo "	or press ENTER to accept the default"
		echo "*****************************************************************"
		echo -n "Postgres installation path: "
		read ans
		if [ "$ans" = "" ]
		then
			return
		else
			pgpath=$ans
		fi
	fi

	# Now prompt the user to input path
	if [ "$pgpath" = "" ]
	then
		echo -n "Please input Postgres installation path: "
		read pgpath
	fi

	while [ 1 ]
	do
		if [ "$pgpath" = "" ]
		then
			exit 1
		fi
		if [ -d "$pgpath" -a -x "$pgpath/bin/psql" ]
		then
			return
		fi 
		echo "Path $pgpath is not a valid Postgres installation, try again"
		read pgpath
	done
}

# Copy shared lib into Postgres installation
#
# void
#
copy_sharedlib_file() {

	srcPrefix="sharedLib"

	os=`get_os`
	srcFile="$srcPrefix/alpine_miner."${os}".so"
	destDir="$pgpath/lib/postgresql"

	echo "*****************************************************************"
	echo "	Path $destDir is the Postgres lib path to copy alpine_miner.so to? (y/n) "
	echo "	Provide the path of Postgres lib to copy alpine_miner.so to"
	echo "	or press ENTER to accept the default"
	echo "*****************************************************************"
	echo -n "Postgres lib path: "
	read ans
	if [ "$ans" != "" ]
	then
		destDir=$ans
	fi

	destFile="$destDir/alpine_miner.so"

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

}

# Read postgre port
#
#void
#
setup_postgres_port(){
	echo "*****************************************************************"
	echo "	Provide the Postgres Database port"
	echo "	or press ENTER to accept the default: $port"
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
		CMD="$pgpath/bin/psql"
	fi

	default_db="template1"
	echo "*****************************************************************"
	echo "	Provide the Postgres Database name"
	echo "	or press ENTER to accept the default: $default_db"
	echo "*****************************************************************"
	echo -n "Database name [$default_db]: "
	read ans
	if [ "$ans" != "" ]
	then
		default_db=$ans
	fi

	setup_postgres_port;

	echo $CMD -p $port -d $default_db -c '"CREATE PROCEDURAL LANGUAGE plpgsql;"'
	$CMD -p $port -d $default_db -c 'CREATE PROCEDURAL LANGUAGE plpgsql;'
	echo $CMD -p $port -d $default_db -f alpine_miner_setup.sql
	$CMD  -p $port -d $default_db -f alpine_miner_setup.sql

	echo "*****************************************************************"
	echo "	Would you like to install Alpine Miner demo database? (y/n) "
	echo "*****************************************************************"
	echo -n "Answer [y]: "
	read ans
	if [ "$ans" != "n" ]
	then
		$CMD  -p $port -d $default_db -f create_demo_db.sql
	fi
	
}

main () {
	show_license
	validate_postgres_install
#	is_relative $pgpath
#	if [ $? -eq 0 ]
#	then
#		pgpath="./$pgpath"
#	fi
	copy_sharedlib_file
	setup_demo_db
	echo "Done."
}

main "$@"



