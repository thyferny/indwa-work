#! /bin/sh 

#  NAME
#       install_web30.sh
#
#  COPYRIGHT
#       Confidential Property of Alpine Data Labs.
#       Copyright (c) 2011 by Alpine Data Labs.
#       All rights reserved.
#
#  DESCRIPTION
#       Install script for Alpine Illuminator Web 3.0.
#
#  The install script will
#
#  1. Validate Tomcat installation and ask the user
#     to confirm.
#
#  2. Copy WAR file into tomcat directory
#
#

tomcatpath=""
product="AlpineIlluminator"
envfile=""
startcmd=""
stopcmd=""

short_license_prompt() {
echo ""
echo "......"
echo ""
echo "********************************************************************************"
echo "  Do you accept the Alpine Illuminator license agreement? [yes | no]"
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


# Determines the OS o the machine
#
#
get_os() {
        MYID=`id -u`

        if [ "$MYID" != "0" ]
        then
                # unknown OS
                echo "Must be the root user to run install, exiting."
                exit 1
        fi
}

# Get default Tomcat install path
#
# void.
#
get_default_tomcat_install() {
        tomcatpath="/usr/share/tomcat6"

        if [ -d "$tomcatpath" -a -d "$tomcatpath/webapps" ]
        then
                return
        fi

        tomcatpath="/usr/local/tomcat"

        if [ -d "$tomcatpath" -a -d "$tomcatpath/webapps" ]
        then
                return
        fi

        # not found
        tomcatpath=""
}

# Validate Tomcat install path
#
# void.
#
validate_tomcat_install() {

        get_default_tomcat_install

        if [ "$tomcatpath" != "" ]
        then
                echo "*****************************************************************"
                echo "  Alpine Illuminator Installer has detected Tomcat istallation"
                echo "  path: $tomcatpath"
                echo ""
                echo "  Provide the installation path for Tomcat "
                echo "  or press ENTER to accept the default"
                echo "*****************************************************************"
                echo -n "Tomcat installation path: "
                read ans
                if [ "$ans" = "" ]
                then
                        return
                else
                        tomcatpath=$ans
                fi
        fi

        # Now prompt the user to input path
        if [ "$tomcatpath" = "" ]
        then
                echo -n "Please input Tomcat installation path: "
                read tomcatpath
        fi

        while [ 1 ]
        do
                if [ "$tomcatpath" = "" ]
                then
                        exit 1
                fi
                if [ -d "$tomcatpath" -a -d "$tomcatpath/webapps" ]
                then
                        return
                fi
                echo "Path $tomcatpath is not a valide Tomcat installation, try again"
                read tomcatpath
        done
}

# Setup data repository
#
# void
#
setup_data_repository() {
        default_data_path=""
        id tomcat > /dev/null 2>&1
        if [ "$?" = "0" ]
        then
                default_data_path=`echo ~tomcat`/ALPINE_DATA_REPOSITORY
	   else
                default_data_path=`echo ~`/ALPINE_DATA_REPOSITORY
        fi
        echo "*****************************************************************"
        echo "  Alpine Illuminator Installer will use the following to store data"
        echo "  path: $default_data_path"
        echo ""
        echo "  Provide a path name to store data"
        echo "  or press ENTER to accept the default"
        echo "*****************************************************************"
        echo -n "Data path: "
        read ans
        if [ "$ans" != "" ]
        then
                default_data_path=$ans
        fi

        if [ ! -d ${default_data_path} ]
        then
                mkdir -p ${default_data_path}
        fi


#	   datafile=`pwd`/data.tgz
#	   cd ${default_data_path} 
#	   tar zxf ${datafile}
#	   cd -
		cp -rf `pwd`/ALPINE_DATA_REPOSITORY/**  ${default_data_path}
}

# TBD
#
# void
#
install_tomcat6() {
        cd /etc/yum.repos.d/
        wget 'http://www.jpackage.org/jpackage50.repo'
        yum update
        yum install tomcat6-webapps tomcat6-admin-webapps
        /etc/init.d/tomcat6 start
}

# Copy war file into Tomcat installation
#
# void
#
copy_war_file() {
        base=${tomcatpath}/webapps
        warfile=${base}/${product}.war

        if [ -f ${warfile} ]
        then
                rm -fr  ${warfile}
                rm -fr  ${base}/${product}
        fi

        cp -f ./${product}.war  ${warfile}
}

restart_tomcat() {
	echo "stop tomcat"
	${stopcmd} > /dev/null 2>&1
	sleep 5

        grep ALPINE_DATA_REPOSITORY "$envfile" > /dev/null 2>&1
        if [ "$?" = "0" ]
        then
                tmpfile=yyxxaa.$!
                grep -v  ALPINE_DATA_REPOSITORY "$envfile" > /tmp/${tmpfile}
                mv /tmp/${tmpfile} "$envfile"
        fi

        echo "export ALPINE_DATA_REPOSITORY=${default_data_path}" >> "$envfile"
	copy_war_file

	echo "start tomcat"
	${startcmd}
	sleep 2

	retry=1
	while [ "$retry" = "1" ]
	do
		ps -ef | grep tomcat | grep -v "grep tomcat" > /dev/null 2>&1
		retry=$?
		if [ "$retry" = "0" ]
		then
			break
		fi
		${startcmd}
		sleep 2
	done
}

main () {
        #get_os
        show_license
        validate_tomcat_install

	# check if it is a yum install or untar.
	if [ -x ${tomcatpath}/bin/startup.sh ]
	then
		envfile=${tomcatpath}/bin/setenv.sh
		startcmd="${tomcatpath}/bin/startup.sh"
		stopcmd="${tomcatpath}/bin/shutdown.sh"
	else
		envfile=/etc/tomcat6/tomcat6.conf
		startcmd="/etc/init.d/tomcat6 start"
		stopcmd="/etc/init.d/tomcat6 stop"
		
	fi

        setup_data_repository

        echo "*****************************************************************"
        echo "  Tomcat needs to be restarted."
        echo "*****************************************************************"
        echo -n "Restart tomocat? (y/n) [y]: "
        read ans
        if [ "$ans" != "n" ]
        then
                restart_tomcat
	else
		echo "You may need to restart tomcat manually"
		copy_war_file
        fi
        echo "Done."
}

main "$@"

