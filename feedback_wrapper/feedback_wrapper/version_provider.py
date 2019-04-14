from git import Repo

def get_current_version(repo_path):
    repo = Repo(repo_path)
    return repo.commit('HEAD').hexsha
