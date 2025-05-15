
# Parameters
# $1 is the target directory for the certificate
# $2 is the name of the bounty
# $3 is the host

cd $1

/usr/local/Cellar/openssl@3/3.2.1/bin/openssl req -x509 -newkey rsa:4096 -keyout $3.key -out $3.crt -days 3650 -nodes  -addext "basicConstraints=CA:FALSE"  -addext "subjectAltName = DNS:$3" -subj /C=US/ST=NewYork/L=/O=/OU=/CN=$3

/usr/local/Cellar/openssl@3/3.2.1/bin/openssl pkcs12 -export -in $3.crt -inkey $3.key -name $3 -out $3.p12

keytool -importkeystore -deststorepass mypass -destkeypass mypass -destkeystore $1/$2.jks -srckeystore $3.p12 -srcstoretype PKCS12 -srcstorepass mypass -alias $3


# Make it der, derrrr
/usr/local/Cellar/openssl@3/3.2.1/bin/openssl x509 -inform pem  -in $3.crt -out $3.der -outform der

sudo security add-certificates -k ~/Library/Keychains/login.keychain-db $3.der

sudo security add-certificates -k /Library/Keychains/System.keychain $3.der

sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain $3.der

#sudo security add-trusted-cert -d -r trustRoot -k ~/Library/Keychains/login.keychain-db -e hostnameMismatch -s sslPolicy $3.crt


