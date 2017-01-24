
def getBranches() {
  def branch_output = bat script : 'git branch -a --sort=-committerdate',
                      returnStdout : true

  def branches = []
  def branch_lines = branch_output.split("\n")
  for (ii = 0; ii < branch_lines.size() && ii < 10; ii++) {
    def line = branch_lines[ii]
    def matcher = line =~ 'remotes/origin/(.+)'
    if (matcher) {
      branches.add(matcher[0][1])
    }
  }
  if (branches.size() == 0) {
     error "ブランチが取得できません : ${branch_output}"
  }
  return branches
}

@NonCPS
def getConfigFiles() {
  def workspace = pwd()
  def config_files = []
  new File("${workspace}/config").eachFile { config_file ->
    def matcher = config_file.name =~ '(config.*\\.groovy)'
    if (matcher) {
      config_files.add(matcher[0][0])
    }
  }
  if (config_files.size() == 0) {
     error "config ファイルが取得できません : ${branch_output}"
  }
  return config_files
}

return this;
