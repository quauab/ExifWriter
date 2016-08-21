# Jpeg Metadata Writer
Write new data to jpeg's exchange information. This project contains an abstract class and a concrete sub-class. The parent has 4 methods that facilitates editing jpeg files: remove a specific tag, edit a specific tag, edit & remove specific tags and remove all tag values.

<h2>Installation</h2>
<ol>
  <li>Download the project's zip</li>
  <li>Compile project</li>
  <li>Generate jar library</li>
  <li>Add jar as external library</li>
  <li>Call from the concrete class or extend it's parent</li>
</ol>

<h2>Dependencies</h2>
<ul>
  <li>Sanselan 0.97-incubator</li>
  <li>ExifReader</li>
</ul>

<h2>Motivation</h2>
Worked a previous project where the program can read jpeg's metadata. I was inspired to create a Java library that could edit a jpeg's exchange information and uploaded to Github as reference or possible Java library for others.

<h2>Issues</h2>
Editing the GPS tags produces an "unknown tag" discrepancy but the changes will still succeed.
