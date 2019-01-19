## Distributed training of Word2Vec
Distributed training of a word embedding model (Word2vec) for large vocabularies

### Quick Start
(instructions added later)

### Project Idea 
Many modern natural language (NLP) models rely on word embeddings. Embeddings are vectors of various sizes (ranging from 50 to 1000 floats most usually) that represent a given word. It is common for a word embedding model to be trained on very large corpuses of text, producing embeddings for a massive vocabulary. One popular model consists of 3 million vectors, each of length 300, trained on Wikipedia. Because these vectors are hugely helpful for Machine Learning, they have been extended to be used on other types of data, such as advertisement data. These datasets are even larger, which can make training hard, or even impossible. 


### Tech Stack
- Spark: distributed processing, fine-grained load balancing, failure recovery, in-memory operations. 
- Parameter Server (PS): Used typically for sync models, incremental updates during training, or sometimes even some vector math. 



Architecture and Features of Parameter Server (PS):
PS nodes are grouped into a server group, and each node maintains a partition of the globally shared parameters (the word vectors)
A server manager node maintains a consistent view of the metadata of the servers, such as node liveness and the assignment of parameter partitions.
A group of workers each stores locally a portion of the training data to compute local statistics such as gradients, communicate with the PS nodes, updating and retrieving parameters.
Shared parameters on PS are presented as (key, value) vectors to facilitate linear algebra operations, such as vector addition, multiplication, and dot product. Keys are ordered to facilitate the implementation of optimization algorithms, in order to leverage CPU-efficient multithreaded linear algo libs like BLAS, LAPACK and ATLAS. 
key-value pairs stored using consistent hashing. For fault tolerance, entries are replicated using chain replication. Different from prior (key,value) systems, the parameter server is optimized for range based communication with compression on both data and range based vector clocks.  
Allow range push and pull request. (mentioned in literature but not sure if relevant here)
Asynchronous tasks. No locking or synchronization needed during parameter update.
Data consistency. It’s always a trade-off between system efficiency (assuming independent tasks for best use of CPU, etc) and algorithm convergence which depend on its sensitivity to data consistency.   In the word2vec case, since each mini-batch only updates a small portion of word vectors, I expect to see minimal impact of the impact due to data consistency.
Messaging: A message consists of a list of (key,value) pairs in the key range R and the associated range vector clock. fast Snappy compression library to compress messages, effectively removing the zeros. Note that key- caching and value-compression can be used jointly.

![parameter server](https://github.com/haoyangOxford/distributed-word2vec/blob/master/parameter%20servers.png)

### Data Source
 - Common Crawl: A corpus of web crawl data composed of over 25 billion web pages hosted on [AWS S3 Bucket](https://registry.opendata.aws/commoncrawl/) and updated monthly. It includes ~7TB, ~71k files of parsed WET files only with plain-text (html tags removed), and an example python script running on spark.

 - [Reddit data](https://bigquery.cloud.google.com/table/fh-bigquery:reddit_comments.all): Comments on Reddit Between 2005 - 2018.
 
### Engineering Challenge
Training word vectors on a single machine is a well studied problem. The goal of this project is to build a pipeline that is distributed to both:
Build vectors for sets that are too large to fit in memory (such as the ad dataset mentioned in the paper)
Have the ability to train many word vector models in parallel on multiple different datasets

## Business Value
Web scale advertising platforms, search engines and personalized recommendation systems rely on large scale and fast training of word or social embeddings.

### MVP
 - ETL for preprocessing input text files 
 - Setting up a parameter for storing and updating word rectors
 - Build a scalable distributed training pipeline for word vectors on reasonable sized data.


### Stretch Goals
As a stretch goal, expose an API where anyone can submit a large dataset (for example a link to an S3 bucket), and train embeddings on it. A good place to start would be by re-implementing this paper on Spark/Hadoop. You could for-example compare its results with a naive implementation of word vectors (in particular the one in SparkMlib, but they cite 4 different ones).

