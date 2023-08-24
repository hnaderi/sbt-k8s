import sbt._
import org.typelevel.sbt.TypelevelSitePlugin
import laika.helium.config._
import laika.ast.Path.Root
import TypelevelSitePlugin.autoImport._
import cats.data.NonEmptyList

object WebsitePlugin extends AutoPlugin {
  override def requires: Plugins = TypelevelSitePlugin

  private val relatedProjectLinks = NonEmptyList
    .of(
      "scala k8s" -> url("https://github.com/hnaderi/scala-k8s"),
      "sbt" -> url("https://github.com/sbt/sbt")
    )
    .map { case (title, url) => TextLink.external(url.toString, title) }

  override def projectSettings: Seq[Setting[_]] = Seq(
    tlSiteHelium ~= {
      _.site
        .topNavigationBar(
          homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home)
        )
        .site
        .mainNavigation(appendLinks =
          Seq(ThemeNavigationSection("Related projects", relatedProjectLinks))
        )
    }
  )

}
