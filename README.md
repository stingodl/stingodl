## StingoDL
StingoDL is a GUI desktop downloader for ABC iView and SBS On Demand content in Australia. These services are geolocked to Australia, so this software will not work elsewhere.
FFmpeg is required to be installed for the conversion of HLS stream to MP4 file.
### Latest Issues
Both ABC and SBS recently implemented changes that broke StingoDL. The SBS issue now appears to be fixed, but The ABC issue (no sound in recent episodes) is being worked on now. This may require a significant change to StingoDL and will take some time.
### Is it legal?
Let's get this out of the way at the beginning. ABC and SBS lawyers have argued that this type of software violates the copyright of media hosted on their sites. However there is a significant body of case law in a number of western jurisdictions going back several decades that casts doubt on the claim when material is recorded purely for personal use. In ancient times (anyone remember VHS cassettes?) this was known as 'time shifting'. What is crystal clear is that **distribution** of copyright material is definitely illegal. And we are not just talking about putting videos up on youtube. Even airdropping your friend a missed episode of Death in Paradise is technically distribution. So please, don't spoil things for everyone.
### A word of caution
StingoDL uses undocumented APIs to list series and episodes on each of the services. Occasionally the available data does not match our current understanding of the API, or the API itself is changed by the service provider and StingoDL will fail. We will make our best effort to fix the software and reissue a new release. You can create a new Issue to bring a problem to our attention if it is not logged already .
### Build
StingoDL is an open source software development project, not a software publisher. We do not provide executables, and building them is a relatively technical task using Java build tools. The minimum requirement is a recent Java JDK (Java 11 or later), the OpenJFX SDK (version 11 or later) and a recent FFmpeg installation.
