import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset

final String keyword = "encodekey1234567"
SecretKeySpec key = new SecretKeySpec(keyword.getBytes(Charset.forName("UTF-8")), "AES")

// 文字列を暗号化 → 復号化
def target = "testtest_test"
def encodeKey = crypt(target.getBytes(), key)
println new String(decrypt(encodeKey, key))

// ファイルを暗号化 → 復号化
def fileByte = new File("config.groovy").getBytes()
def encFile = crypt(fileByte, key)

new File("config_encrypt.groovy").setBytes(encFile)

def decFile = decrypt(encFile, key)
new File("config_decrypt.groovy").setBytes(decFile)

// 暗号化
def crypt(byte[] bytes, SecretKeySpec key) {
  def cph = Cipher.getInstance("AES")
  cph.init(Cipher.ENCRYPT_MODE, key)

  return cph.doFinal(bytes)
}

// 復号化
def decrypt(byte[] bytes, SecretKeySpec key) {
  def cph = Cipher.getInstance("AES")
  cph.init(Cipher.DECRYPT_MODE, key)

  return cph.doFinal(bytes)
}
