const regex = /^(\s+versionName\s+)'(\d+\.\d+\.\d+)'$/m

module.exports.readVersion = function (contents) {
  return contents.match(regex)[2]
}

module.exports.writeVersion = function (contents, version) {
  return contents.replace(regex, `$1'${version}'`)
}
