import setuptools

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setuptools.setup(
    name="unidef",
    version="0.2.0",
    author="Jiangkun QIU",
    author_email="qjk2001@gmail.com",
    description="Define once, run everywhere",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/qiujiangkun/unidef",
    project_urls={
        "Bug Tracker": "https://github.com/qiujiangkun/unidef/issues",
    },
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    package_dir={"": "unidef"},
    packages=setuptools.find_packages(where="unidef"),
    python_requires=">=3.6",
)