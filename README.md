# A java to cpp translator

## Git instructions

###### To start.

1. Fork a repo through the gitlab website.

2. Go into your terminal and "git clone [your fork's url]". example: https://gitlab.com/seankwon/midtermproject.git

3. Then do the following commands

    - git remote add upstream https://gitlab.com/nyu-oop-fa14-whiteknights/midtermproject.git
    - git branch develop

##### How our git process will work

How the git heirarchy will work is that we will be programming from our
separate repos and merging changes into the main "master" repo when we're
finished with the features we are developing. ALWAYS CODE IN YOUR DEVELOP
BRANCH.

1. Before you start coding, make sure do the following commands in this exact
   order

    - git checkout master && git pull upstream master

2. Make changes in your branch

3. When done making changes, commit your changes, merge changes into your
   develop branch. In order to push changes to the repo remotely do the
   following...
    
    - git add .
    - git commit -m "some message"
    - git push origin master
    

4. Then "git pull upstream master" so that changes can be up to date and we can avoid
   merge conflicts.

5. Then submit a merge request through gitlabs

6. Repeat this cycle once your done.

##### How to make a merge request

1. On gitlab, go to your fork of the main repo [username]/MidtermProject and click the merge request tab.

2. Click new merge request. Make sure the source branch dropdown selects your repo, and the target branch dropdown "nyu-oop-fa14-whiteknights/midtermproject" and the branch selects "develop" 

3. You will then be redirected with a page asking for a title and description about your latest changes. Make sure you're very detailed on what changes you've made. Write comments in the diff if necessary.

4. Then click merge request.

##### Once your merge request (or anyone elses) is accepted.

Type the following commands

    - git checkout master && git pull upstream master

It is imperative that we pull constantly or we will step on each others toes

##### Basic Git Commands

To create a branch

    - git branch [name of branch]
    
To move into another branch

    - git checkout [name of branch]

To stash changes made on a branch instead of commiting them

    - git stash

To merge changes from one branch to another

    - git checkout [branch you want to merge changes in]
    - git merge [source branch]


#How to compile

1. "make program"
2. "java Sentinel [name of java test file]"
3. "g++ main.h"
4. "g++ main.cc"
5. "./a.out"
3. done
