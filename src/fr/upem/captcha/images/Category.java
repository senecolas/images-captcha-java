/**
 * @authors : Olivier MEYER - Nicolas SENECAL
 * @date : 2019/05/06
 * @file : Category.java
 * @package : fr.upem.captcha.images
 */
package fr.upem.captcha.images;

import fr.upem.captcha.images.CategoryTools;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Category {

  /**
   * URL list of all images present directly in the current folder
   */
  private ArrayList<URL> currentImages;
  /**
   * List of instances of each subcategories
   */
  private ArrayList<Category> categories;

  /**
   * Constructor, populate all subcategories and current images Catch and show
   * IOException if there are errors
   */
  protected Category() {
    this.currentImages = new ArrayList<URL>();
    this.categories = new ArrayList<Category>();
    try {
      this.populateCurrentImages();
      this.populateCategories();
    } catch (IOException ex) {
      Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Get currentImages copy
   *
   * @return Return the URL list of all images present directly in the current
   * folder
   */
  public ArrayList<URL> getCurrentImages() {
    return (ArrayList<URL>) currentImages.clone();
  }

  /**
   * Get all images of the category
   *
   * @return the URL list of all images of the category, child included
   */
  public ArrayList<URL> getImages() {
    return this.getImages(new ArrayList<Category>());
  }

  /**
   * Get all images of the category, ignoring a sub category instance
   *
   * @param excluded category instance to exclude
   * @return the URL list of all images of the category, child included
   */
  public ArrayList<URL> getImages(Category excluded) {
    ArrayList<Category> excludedList = new ArrayList<Category>();
    excludedList.add(excluded);
    return this.getImages(excludedList);
  }

  /**
   * Get all images of the category, ignoring a sub category instance
   *
   * @param count number of images to get
   * @param excludedList list of categories instance to exclude
   * @return a list of random images
   */
  public ArrayList<URL> getImages(ArrayList<Category> excludedList) {
    ArrayList<URL> images = new ArrayList<URL>();

    // add sub categories images
    for (Category category : categories) {
      if (!excludedList.contains(category)) {
        images.addAll(category.getImages());
      }
    }

    // add current images
    images.addAll(this.getCurrentImages());

    return images;
  }

  /**
   * Get the current directory path
   *
   * @return Return the absolute path of the current directory
   */
  private Path getCurrentPath() {
    String className = this.getClassFileName();
    URL url = this.getClass().getResource(className); // URL of the actual class file
    File file = new File(url.getPath()); // Actual class file
    return Paths.get(file.getParent()); // Path of the parent
  }

  /**
   * Get the name of the current class file
   *
   * @return Return the current class file
   */
  private String getClassFileName() {
    return this.getClass().getSimpleName() + ".class";
  }

  /**
   * Populate "currentImages" array with all the image files present in the
   * current folder
   */
  private void populateCurrentImages() throws IOException {
    List<String> images = new ArrayList<String>();
    Path currentPath = this.getCurrentPath();

    images = Files.walk(currentPath, 1)
            .map(Path::getFileName)
            .map(Path::toString)
            .filter(n -> n.contains(".jpg") || n.contains(".jpeg") || n.contains(".png"))
            .collect(Collectors.toList());

    for (String image : images) {
      currentImages.add(this.getClass().getResource(image));
    }
  }

  /**
   * Get the list of name of subFolders of current path
   *
   */
  private List<String> getSubFolders() throws IOException {
    List<String> directories = new ArrayList<String>();
    Path currentPath = this.getCurrentPath();

    directories = Files.walk(currentPath, 1)
            .map(Path::getFileName)
            .map(Path::toString)
            .filter(n -> !n.contains("."))
            .collect(Collectors.toList());
    directories.remove(0); // We remove the current folder
    return directories;
  }

  /**
   * Populate "categories" array with all the Categories in sub folders
   *
   */
  private void populateCategories() throws IOException {
    List<String> subFolders = this.getSubFolders();
    Path currentPath = this.getCurrentPath();

    for (String subFolder : subFolders) {
      Path subFolderPath = Paths.get(currentPath + "/" + subFolder);
      String packageName = this.getClass().getPackage().getName() + "." + subFolderPath.getFileName();

      List<String> classNames = Files.walk(subFolderPath, 1)
              .map(Path::getFileName)
              .map(Path::toString)
              .filter(n -> n.contains(".class")) // get class file
              .map(n -> packageName + "." + n.replace(".class", "")) // get class full name (with package)
              .collect(Collectors.toList());

      for (String className : classNames) {
        Object classObject = null;
        try {
          classObject = Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
          Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
          Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
          Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
          Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
          Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
          Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If classObject is a child of the current class
        if (this.getClass().isInstance(classObject)) {
          categories.add((Category) classObject); // add to child categories
        }
      }
    }
  }

  /**
   * Get the name description of the category
   *
   * @return the name description of the category
   */
  abstract public String getName();

  /**
   * Override equals function
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Category other = (Category) obj;
    if (getName() == null) {
      if (other.getName() != null) {
        return false;
      }
    } else if (!getName().equals(other.getName())) {
      return false;
    }
    return true;
  }

  /**
   * Override toString method for display category informations
   *
   * @return the string description of the category
   */
  @Override
  public String toString() {
    return this.toString(0);
  }

  /**
   * ToString method for display category informations with tabulation for
   * clarity
   *
   * @param nbTab tab number before display category informations
   * @return the string description of the category
   */
  public String toString(int nbTab) {
    StringBuilder str = new StringBuilder();
    str.append("\n");

    // Category name and description 
    str.append(CategoryTools.repeat("  ", nbTab));
    str.append("=== ");
    str.append(this.getClass().getSimpleName());
    str.append(" - ");
    str.append(this.getName());
    str.append(" ===");

    // Category current images
    str.append("\n");
    str.append(CategoryTools.repeat("  ", nbTab));
    str.append("  Images numbers = ");
    str.append(currentImages.size());

    // Category sub cattegories
    str.append("\n");
    str.append(CategoryTools.repeat("  ", nbTab));
    str.append("  sub categories numbers = ");
    str.append(categories.size());
    str.append("\n");
    if (categories.size() > 0) {
      str.append(CategoryTools.repeat("  ", nbTab));
      str.append("  sub categories {");
      for (Category category : categories) {
        str.append("\n");
        str.append(category.toString(nbTab + 2));
      }
      str.append("\n");
      str.append(CategoryTools.repeat("  ", nbTab));
      str.append("  }");
    }
    return str.toString();
  }
}
