# Parameters
# $1 is the target directory for the certificate
# $2 is the name of the bounty
# $3 is the host

rm $1/$2.jks

keytool -genkey -alias temp -keyalg RSA -keystore $1/$2.jks -storepass mypass -dname "CN=Temp, OU=Temp, O=Temp, L=Temp, S=Temp, C=US" -keypass mypass
