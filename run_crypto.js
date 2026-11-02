const crypto = require('crypto');

try{
    const accessKey = "booking_travel_thuong_crypto@seaweedfs_access_key"
    const secretKey = "booking_travel_thuong_crypto@seaweedfs_secret_key"
    const STS = "booking_travel_thuong_crypto@seaweedfs_jwt_filer_signing_key@"
    const hashAK = crypto.createHash('sha256').update(accessKey).digest('hex')
    const hashSK = crypto.createHash('sha256').update(secretKey).digest('hex')
    const hashSTS = crypto.createHash('sha256').update(STS).digest('hex')

    console.log(`Access Key: ${accessKey}, Hash: ${hashAK}`)
    console.log(`Secret Key: ${secretKey}, Hash: ${hashSK}`)
    console.log(`STS: ${STS}, Has: ${hashSTS}`)
} catch (err) {
    console.error('Crypto operation failed:', err.message);
}